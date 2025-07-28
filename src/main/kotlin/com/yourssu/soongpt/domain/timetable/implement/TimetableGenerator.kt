package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.implement.preferWeekday
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.rating.implement.RatingReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCourseResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidates
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private const val MAXIMUM_TIMETABLE_CANDIDATES = 1000
@Component
class TimetableGenerator (
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val ratingReader: RatingReader,
    private val courseCandidateFactory: CourseCandidateFactory,
    private val timetableWriter: TimetableWriter,
    private val timetableCourseWriter: TimetableCourseWriter,
){
    @Transactional
    fun issueTimetables(timetableCandidates: List<TimetableCandidate>): List<TimetableResponse> {
        val responses = ArrayList<TimetableResponse>()
        for (candidate in timetableCandidates) {
            val tag = candidate.validTags.getOrNull(0) ?: Tag.DEFAULT
            val score = calculateScore(candidate)
            val timetable = timetableWriter.save(
                Timetable(
                    tag = tag,
                    score = score,
                )
            )
            saveTimetableCourses(candidate.codes, timetable)
            responses.add(
                TimetableResponse.from(
                    timetable = timetable,
                    courses = generateTimetableCourseResponses(candidate.codes),
                )
            )
        }
        return responses
    }
    private fun saveTimetableCourses(codes: List<Long>, timetable: Timetable) {
        val courses = courseReader.groupByCategory(codes)
        val flattenCourses = getFlattenCoursesInCourseGroup(courses)
        for (course in flattenCourses) {
            timetableCourseWriter.save(TimetableCourse(timetableId = timetable.id!!, courseId = course.id!!))
        }
    }
    fun generate(command: TimetableCreatedCommand): List<TimetableCandidate> {
        val department = departmentReader.getByName(command.departmentName)
        val baseCourseCandidates = generateCourseCandidatesForBase(command, department)
        val baseTimeTables = generateBaseTimetable(baseCourseCandidates)
        val baseBuilders: List<TimetableCandidateBuilder> = baseTimeTables.flatMap { it.toBuilders() }
        val timetableBuilders = generateFinalTimeTables(baseBuilders, command, department)

        val finalTimetables = timetableBuilders
            .map { it.build() }
            .filter { it.validTags.isNotEmpty() && it.points > 0 }

        val topTimetablesByTag = groupAndSelectTopN(finalTimetables, 2)
        val topTimetableCandidates = topTimetablesByTag.values.flatten()
        return topTimetableCandidates
    }

    private fun generateTimetableCourseResponses(
        codes: List<Long>
    ): List<TimetableCourseResponse> {

        val flattenCourses = getFlattenCoursesInCourseGroup(
            courseReader.groupByCategory(codes)
        )

        val timetableCourseResponses = flattenCourses.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
            TimetableCourseResponse.from(course, courseTimes)
        }
        return timetableCourseResponses
    }

    private fun calculateScore(candidate: TimetableCandidate): Int {
        val score = candidate.validTags.size * 10
        return score
    }
    private fun groupAndSelectTopN(
        candidates: List<TimetableCandidate>,
        n: Int,
    ): Map<Tag, List<TimetableCandidate>> {
        return candidates
            .flatMap { candidate ->
                candidate.validTags.map { tag -> tag to candidate }
            }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )
            .mapValues { (_, timetablesInGroup) ->
                timetablesInGroup
                    .shuffled()
                    .sortedByDescending { candidate ->
                        candidate.points
                    }
                    .take(n)
            }
    }


    private fun generateFinalTimeTables(
        timeTableBuilders: List<TimetableCandidateBuilder>,
        command: TimetableCreatedCommand,
        department: Department
    ): List<TimetableCandidateBuilder> {
        if (!command.isChapel) {
            val tables = addGeneralElectives(timeTableBuilders, command, department)
            return tables
        }

        if (command.grade == 1) {
            val mandatoryChapelCandidates = findChapelCandidates(department, command.grade)
            val tablesWithChapel = addChapel(timeTableBuilders, mandatoryChapelCandidates)
            val tables = addGeneralElectives(tablesWithChapel, command, department)
            return tables
        } else {
            val tables = addGeneralElectives(timeTableBuilders, command, department)
            val optionalChapelCandidates = findChapelCandidates(department, command.grade)
            val tablesWithChapel = addChapel(tables, optionalChapelCandidates)
            return tablesWithChapel
        }
    }

    private fun addGeneralElectives(
        builders: List<TimetableCandidateBuilder>,
        command: TimetableCreatedCommand,
        department: Department
    ): List<TimetableCandidateBuilder> {
        if (command.generalElectivePoint <= 0) {
            return builders
        }

        val sortedElectiveCandidates = generateSortedElectiveCandidates(department, command)
        return builders.map { builder ->
            var remainingPoints = command.generalElectivePoint

            for (elective in sortedElectiveCandidates) {
                if (remainingPoints <= 0) {
                    break
                }
                if (elective.point > remainingPoints) {
                    continue
                }

                val wasAdded = builder.add(elective)
                if (wasAdded) {
                    remainingPoints -= elective.point
                }
            }
            builder
        }
    }

    private fun generateSortedElectiveCandidates (
        department: Department,
        command: TimetableCreatedCommand
    ): List<CourseCandidate> {
        val allCourses = courseReader.findAllBy(
            category = Category.GENERAL_ELECTIVE,
            department = department,
            grade = command.grade
        ).preferWeekday()

        val (preferredCourses, otherCourses) = allCourses.partition { course ->
            command.preferredGeneralElectives.isNotEmpty() &&
            command.preferredGeneralElectives.any { pref ->
                course.field?.contains(pref) == true
            }
        }

        val codeToRank = ratingReader.findAll()
            .sortedByDescending { it.star }
            .mapIndexed { idx, rating -> rating.code to idx }
            .toMap()

        fun List<CourseCandidate>.sortByPointDescStarDesc() =
            sortedWith(
                compareByDescending<CourseCandidate> { it.point }
                    .thenBy { codeToRank[it.code] ?: Int.MAX_VALUE }
            )

        val preferredCandidates = preferredCourses.map(courseCandidateFactory::create)
        val otherCandidates = otherCourses.map(courseCandidateFactory::create)

        val sortedPreferred = preferredCandidates.sortByPointDescStarDesc()
        val sortedOthers = otherCandidates.sortByPointDescStarDesc()

        if (sortedPreferred.isNotEmpty()) {
            return sortedPreferred + sortedOthers
        }
        else {
            return (preferredCandidates + otherCandidates).sortByPointDescStarDesc()
        }
    }

    private fun addChapel(
        baseBuilders: List<TimetableCandidateBuilder>,
        chapelCandidates: CourseCandidates,
    ): List<TimetableCandidateBuilder> {
        // TODO: chapel test
        return baseBuilders.map { builder ->
            chapelCandidates.candidates.map {
                builder.add(it)
            }
            builder
        }
    }


    private fun findChapelCandidates(
        department: Department,
        grade: Int,
    ): CourseCandidates {
        val tmp = courseReader.findAllBy(
            category = Category.CHAPEL,
            department = department,
            grade = grade
        )

        val chapelCourses = tmp.map {
            courseCandidateFactory.create(it)
        }
        return CourseCandidates.from(chapelCourses)
    }
    private fun generateCourseCandidatesForBase(
        command: TimetableCreatedCommand,
        department: Department
    ): List<CourseCandidates> {
        val courseCodes = getFlattenCourseCodes(command)
        val courseGroup = courseReader.groupByCategory(courseCodes)
        val flattenCourses = getFlattenCoursesInCourseGroup(courseGroup)

        return generateAllCourseCandidates(flattenCourses, department, command.grade)
    }

    private fun generateBaseTimetable(
        courseCandidates: List<CourseCandidates>
    ): List<TimetableCandidate> {
        val results = mutableListOf<TimetableCandidate>()
        val builder = TimetableCandidateBuilder()

        fun dfs(depth: Int) {
            if (results.size >= MAXIMUM_TIMETABLE_CANDIDATES) {
                return
            }

            if (depth == courseCandidates.size) {
                results.add(builder.build())
                return
            }

            val candidates = courseCandidates[depth].candidates
            for (candidate in candidates) {
                if (builder.add(candidate)) {
                    dfs(depth + 1)
                    builder.remove(candidate)
                }
            }
        }

        dfs(0)
        return results
    }

    private fun getFlattenCourseCodes(command: TimetableCreatedCommand): List<Long> {
        return listOf(
            command.majorRequiredCodes,
            command.majorElectiveCodes,
            command.generalRequiredCodes,
            command.codes,
        ).flatten().distinct()
    }

    private fun getFlattenCoursesInCourseGroup(courseGroup: GroupedCoursesByCategoryDto): List<Course> {
        return listOf(
            courseGroup.majorRequiredCourses,
            courseGroup.generalRequiredCourses,
            courseGroup.majorElectiveCourses,
            courseGroup.generalElectiveCourses,
        ).flatten().distinctBy { it.code }.preferWeekday()
    }

    private fun generateAllCourseCandidates(
        courses: List<Course>,
        department: Department,
        grade: Int,
    ): List<CourseCandidates> {
        val courseCandidates =
            courses.map {
                val classes = courseReader.findAllByClass(
                    department = department,
                    grade = grade,
                    code = it.code,
                )

                CourseCandidates.from(
                    candidates = classes.map { course ->
                        courseCandidateFactory.create(course)
                    }
                )
            }
        return courseCandidates
    }
}