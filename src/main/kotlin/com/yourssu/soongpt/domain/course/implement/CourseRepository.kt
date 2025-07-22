package com.yourssu.soongpt.domain.course.implement

interface CourseRepository {
    fun get(code: Long): Course
    fun getAll(codes: List<Long>): List<Course>
    fun getAllInCategory(category: Category, courseIds: List<Long>): List<Course>
}
