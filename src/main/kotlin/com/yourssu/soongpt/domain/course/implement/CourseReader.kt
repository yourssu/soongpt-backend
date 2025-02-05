package com.yourssu.soongpt.domain.course.implement

import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
) {
    fun findAllByDepartmentIdInMajorRequired(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.MAJOR_REQUIRED)
    }

    fun findAllByDepartmentIdInMajorElective(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.MAJOR_ELECTIVE)
    }

    fun findAllByDepartmentIdInGeneralRequired(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.GENERAL_REQUIRED)
    }

    fun findByCourseNameInMajorRequired(departmentId: Long, courseName: String): Course {
        return courseRepository.findByDepartmentIdAndCourseName(departmentId, courseName, Classification.MAJOR_REQUIRED)
    }

    fun findByCourseNameInMajorElective(departmentId: Long, courseName: String): Course {
        return courseRepository.findByDepartmentIdAndCourseName(departmentId, courseName, Classification.MAJOR_ELECTIVE)
    }

    fun findByCourseNameInGeneralRequired(departmentId: Long, courseName: String): Course {
        return courseRepository.findByDepartmentIdAndCourseName(departmentId, courseName, Classification.GENERAL_REQUIRED)
    }
}
