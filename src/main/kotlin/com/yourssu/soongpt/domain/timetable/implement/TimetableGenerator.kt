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
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.stereotype.Component
import kotlin.random.Random

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

        if (baseTimeTables.isEmpty()) {
            throw TimetableNotFoundException()
        }

        val baseBuilders = baseTimeTables.flatMap { it.toBuilders() }
        val timetableBuilders = generateFinalTimeTables(baseBuilders, command, department)

        val finalTimetables = timetableBuilders
            .map { it.build() }
            .filter { it.validTags.isNotEmpty() && it.codes.isNotEmpty() }

        val topTimetablesByTag = groupAndSelectTopN(finalTimetables, 2)
        val topTimetableCandidates = topTimetablesByTag.values.flatten()

        val distinctFinalTimetableCandidates = removeDuplicateTimetables(topTimetableCandidates)
        if (distinctFinalTimetableCandidates.isEmpty()) {
            throw TimetableNotFoundException()
        }
        return distinctFinalTimetableCandidates
    }

    private fun removeDuplicateTimetables(
        topTimetableCandidates: List<TimetableCandidate>
    ): List<TimetableCandidate> {
        // Tag는 Default가 앞쪽 우선이라, 해당 내용 거르기 위한 reverse
        val reverseCandidates = topTimetableCandidates.reversed()
        val distinctCandidates = mutableListOf<TimetableCandidate>()

        for (candidate in reverseCandidates) {
            var isDistinct = true
            for (distinctCandidate in distinctCandidates) {
                if (candidate.timeSlot.toString() == distinctCandidate.timeSlot.toString() ||
                    candidate.codes.toSet() == distinctCandidate.codes.toSet()) {
                    isDistinct = false
                    break
                }
            }
            if (isDistinct) {
                distinctCandidates.add(candidate)
            }
        }
        return distinctCandidates.reversed()
    }

    private fun generateTimetableCourseResponses(
        codes: List<Long>
    ): List<TimetableCourseResponse> {
        val flattenCourses = courseReader.findAllByCode(codes)
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

    private fun String.normalizeDot(): String =
        replace("""\s*·\s*""".toRegex(), "·")
            .trim()

    private fun sortByPointDescStarDescShuffleEqual(
        candidates: List<CourseCandidate>,
        codeToRank: Map<Long, Int>
    ): List<CourseCandidate> =
        candidates.groupBy { it.point }
            .toList()
            .sortedByDescending { it.first }            // point ↓
            .flatMap { (_, samePoint) ->
                samePoint
                    .groupBy { codeToRank[it.code] ?: Int.MAX_VALUE }
                    .toList()
                    .sortedBy { it.first }              // star ↓ (rank ↑)
                    .flatMap { (_, sameStar) ->
                        if (sameStar.size > 1)
                            sameStar.shuffled(Random(System.nanoTime()))      // 동점만 섞기
                        else sameStar
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

        val electiveFields = courseReader.getFieldsBySchoolId(command.schoolId)
            .filterNot { it.startsWith("교필") }
            .map { it.normalizeDot() }

        val preferredFields = command.preferredGeneralElectives
            .map { it.normalizeDot() }

        val courses = allCourses.filter { course ->
            val field = course.field?.normalizeDot() ?: return@filter false
            electiveFields.any(field::contains)
        }

        val (preferredCourses, otherCourses) = courses.partition { course ->
            preferredFields.isNotEmpty() && preferredFields.any { pref ->
                course.field?.normalizeDot()?.contains(pref) == true
            }
        }

        val codeToRank = ratingReader.findAll()
            .sortedByDescending { it.star }
            .mapIndexed { idx, rating -> rating.code to idx }
            .toMap()

        val preferredCandidates = preferredCourses.map(courseCandidateFactory::create)
        val otherCandidates = otherCourses.map(courseCandidateFactory::create)

        val sortedPreferred = sortByPointDescStarDescShuffleEqual(
            preferredCandidates,
            codeToRank
        )

        if (sortedPreferred.isNotEmpty()) {
            return sortByPointDescStarDescShuffleEqual(
                sortedPreferred,
                codeToRank
            )
        }
        else {
            return sortByPointDescStarDescShuffleEqual(
                preferredCandidates + otherCandidates,
                codeToRank
            )
        }
    }

    private fun addChapel(
        baseBuilders: List<TimetableCandidateBuilder>,
        chapelCandidates: CourseCandidates,
    ): List<TimetableCandidateBuilder> {
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