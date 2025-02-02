package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseReader: CourseReader,
    private val courseTimeReader: CourseTimeReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
) {
    fun findByDepartmentNameInMajorRequired(departmentName: String): List<CourseResponse> {
        val department = departmentReader.getByName(departmentName)
        val courses = courseReader.findAllByDepartmentIdInMajorRequired(department.id!!)
        return courses.map {
            val targets = targetReader.findAllBy(courseId = it.id!!, department = department)
            val courseTimes = courseTimeReader.findAllByCourseId(it.id)
            CourseResponse.from(course = it, target = targets, courseTimes = courseTimes)
        }
    }

    fun findByDepartmentNameInMajorElective(departmentName: String): List<CourseResponse> {
        val department = departmentReader.getByName(departmentName)
        val courses = courseReader.findAllByDepartmentIdInMajorElective(department.id!!)
        return courses.map {
            val targets = targetReader.findAllBy(courseId = it.id!!, department = department)
            val courseTimes = courseTimeReader.findAllByCourseId(it.id)
            CourseResponse.from(course = it, target = targets, courseTimes = courseTimes)
        }
    }
}