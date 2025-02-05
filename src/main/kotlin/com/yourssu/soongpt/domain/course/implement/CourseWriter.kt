package com.yourssu.soongpt.domain.course.implement

import org.springframework.stereotype.Component

@Component
class CourseWriter(
    private val courseRepository: CourseRepository
) {
    fun save(course: Course): Course {
        return courseRepository.save(course)
    }
}