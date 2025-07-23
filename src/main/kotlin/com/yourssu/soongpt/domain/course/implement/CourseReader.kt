package com.yourssu.soongpt.domain.course.implement

import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
) {
    fun findAllInCategory(category: Category, courseIds: List<Long>): List<Course> {
        return courseRepository.findAllByCategoryTarget(category, courseIds)
    }
}
