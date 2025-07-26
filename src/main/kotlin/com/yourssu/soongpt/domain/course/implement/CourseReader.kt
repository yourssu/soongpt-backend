package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
) {
    fun findAllInCategory(category: Category, courseIds: List<Long>): List<Course> {
        return courseRepository.findAllByCategory(category, courseIds)
    }

    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        return courseRepository.groupByCategory(codes)
    }
}
