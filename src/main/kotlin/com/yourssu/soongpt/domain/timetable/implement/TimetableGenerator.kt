package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidates
import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate
import com.yourssu.soongpt.domain.timetable.implement.timeslot.TIMESLOT_SIZE
import org.springframework.stereotype.Component
import java.util.BitSet

@Component
class TimetableGenerator (
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val courseCandidateFactory: CourseCandidateFactory,
){

    fun generate(command: TimetableCreatedCommand): TimetableResponses {
        val courseCodes = getFlattenCourseCodes(command)
        val courseGroup = courseReader.groupByCategory(courseCodes)

        val courseCandidates = generateAllCourseCandidates(
            courseGroup = courseGroup,
            department = departmentReader.getByName(command.departmentName),
            grade = command.grade,
        )

        val timetableCandidates = generateTimetableCandidates(courseCandidates)

        // 교선 넣고
        // strategy 전략 체크 -> 스코어 체크
        // 상위 3개씩 db 저장
        return TimetableResponses(
            listOf()
        )
    }

    private fun generateTimetableCandidates(
        courseCandidates: List<CourseCandidates>
    ): List<TimetableCandidate> {
        val results = mutableListOf<TimetableCandidate>()
        val currentCodes = mutableListOf<Long>()
        val currentSlots = BitSet(TIMESLOT_SIZE)

        fun dfs(depth: Int) {
            if (depth == courseCandidates.size) {
                results.add(TimetableCandidate.from(currentCodes.toList(), currentSlots.clone() as BitSet))
                return
            }

            val candidates = courseCandidates[depth].candidates
            for (candidate in candidates) {
                if (currentSlots.intersects(candidate.timeSlot)) {
                    continue
                }

                currentCodes.add(candidate.code)
                currentSlots.or(candidate.timeSlot)

                dfs(depth + 1)

                currentCodes.removeAt(currentCodes.lastIndex)
                currentSlots.xor(candidate.timeSlot)
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
        ).flatten().distinctBy { it.code }
    }

    private fun generateAllCourseCandidates(
        courseGroup: GroupedCoursesByCategoryDto,
        department: Department,
        grade: Int,
    ): List<CourseCandidates> {
        val flattenCourses = getFlattenCoursesInCourseGroup(courseGroup)

        val courseCandidates =
            flattenCourses.map {
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