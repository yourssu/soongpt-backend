package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto

interface CourseRepository {
    fun get(code: Long): Course
    fun getAll(codes: List<Long>): List<Course>
    fun getAllInCategory(category: Category, courseIds: List<Long>): List<Course>
    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto
}
