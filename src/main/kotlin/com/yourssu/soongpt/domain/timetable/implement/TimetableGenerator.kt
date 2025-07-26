package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidates
import org.springframework.stereotype.Component

@Component
class TimetableGenerator (
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val courseCandidateFactory: CourseCandidateFactory,
){

    fun generate(command: TimetableCreatedCommand): TimetableResponses {
        val selectedCodes = getAllCourseCodes(command)
        val courseGroup = courseReader.groupByCategory(selectedCodes)

        val courseCandidates = getAllCourseCandidates(
            courseGroup = courseGroup,
            department = departmentReader.getByName(command.departmentName),
            grade = command.grade,
        )

        // TODO: 각 그룹별로 TimetableCandidate 생성
        // 이후 교선 처리, 태그 처리

        return TimetableResponses(
            listOf()
        )
    }

    private fun getAllCourseCodes(command: TimetableCreatedCommand): List<Long> {
        return listOf(
            command.majorRequiredCodes,
            command.majorElectiveCodes,
            command.generalRequiredCodes,
            command.codes,
        ).flatten().distinct()
    }

    private fun getAllCourses(courseGroup: GroupedCoursesByCategoryDto): List<Course> {
        return listOf(
            courseGroup.majorRequiredCourses,
            courseGroup.generalRequiredCourses,
            courseGroup.majorElectiveCourses,
            courseGroup.generalElectiveCourses,
        ).flatten().distinctBy { it.code }
    }

    private fun getAllCourseCandidates(
        courseGroup: GroupedCoursesByCategoryDto,
        department: Department,
        grade: Int,
    ): List<CourseCandidates> {
        val allCourses = getAllCourses(courseGroup)

        val courseCandidates =
            allCourses.map {
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