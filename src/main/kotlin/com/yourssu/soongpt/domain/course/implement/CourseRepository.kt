package com.yourssu.soongpt.domain.course.implement

interface CourseRepository {
    fun get(code: Long): Course
    fun findAll(courseIds: List<Long>): List<Course>
    fun findAllByCategory(category: Category, courseIds: List<Long>): List<Course>
}
