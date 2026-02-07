package com.yourssu.soongpt.domain.coursefield.implement

import org.springframework.stereotype.Component

@Component
class CourseFieldReader(
    private val courseFieldRepository: CourseFieldRepository,
) {
    fun findByCourseCode(courseCode: Long): CourseField? {
        return courseFieldRepository.findByCourseCode(courseCode)
    }

    fun findAll(): List<CourseField> {
        return courseFieldRepository.findAll()
    }
}
