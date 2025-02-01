package com.yourssu.soongpt.domain.course.implement

import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
) {
    fun findAllByDepartmentIdInMajorRequired(departmentId: Long): List<Course> {
        return courseRepository.findAllByDepartmentId(departmentId, Classification.MAJOR_REQUIRED)
    }
}
