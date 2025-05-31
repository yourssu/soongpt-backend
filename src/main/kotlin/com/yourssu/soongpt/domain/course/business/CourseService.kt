package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseReader: CourseReader,
    private val courseTimeReader: CourseTimeReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
    private val departmentGradeReader: DepartmentGradeReader,
) {
    @Cacheable(value = ["courseCache"], key = "'majorRequired'.concat(#command.departmentName).concat(#command.grade)")
    fun findByDepartmentNameInMajorRequired(command: FoundDepartmentCommand): List<CourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeIdInMajorRequired(departmentGrade.id!!)
        return filterNotEmptyCourseTimes(courses.map {
            val courseTimes = courseTimeReader.findAllByCourseId(it.id!!)
            CourseResponse.from(
                course = it,
                target = listOf(targetReader.formatTargetDisplayName(department, departmentGrade)),
                courseTimes = courseTimes
            )
        })
    }

    @Cacheable(value = ["courseCache"], key = "'MajorElective'.concat(#command.departmentName).concat(#command.grade)")
    fun findByDepartmentNameInMajorElective(command: FoundDepartmentCommand): List<CourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val courses = courseReader.findAllByDepartmentIdInMajorElective(department.id!!)
        return filterNotEmptyCourseTimes(courses.map { (course, departmentGrades) ->
            val courseTimes = courseTimeReader.findAllByCourseId(course.id!!)
            CourseResponse.from(
                course = course,
                target = departmentGrades.map { targetReader.formatTargetDisplayName(department, it) },
                courseTimes = courseTimes)
        })
    }

    @Cacheable(value = ["courseCache"], key = "'GeneralRequired'.concat(#command.departmentName).concat(#command.grade)")
    fun findByDepartmentNameInGeneralRequired(command: FoundDepartmentCommand): List<CourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeIdInGeneralRequired(departmentGrade.id!!)
        return filterNotEmptyCourseTimes(courses.map {
            val courseTimes = courseTimeReader.findAllByCourseId(it.id!!)
            CourseResponse.from(
                course = it,
                target = listOf(targetReader.formatTargetDisplayName(department, departmentGrade)),
                courseTimes = courseTimes
            )
        })
    }

    private fun filterNotEmptyCourseTimes(courses: List<CourseResponse>): List<CourseResponse> {
        return courses.filter { it.courseTime.isNotEmpty() }
    }

    fun findById(courseId: Long): CourseResponse {
        val course = courseReader.getById(courseId)
        val courseTimes = courseTimeReader.findAllByCourseId(courseId)
        val target = targetReader.findAllByCourseId(courseId)
        return CourseResponse.from(course, target, courseTimes)
    }
}
