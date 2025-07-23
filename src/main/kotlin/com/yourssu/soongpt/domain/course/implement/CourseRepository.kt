package com.yourssu.soongpt.domain.course.implement

interface CourseRepository {
    fun get(code: Long): Course
    fun findAllByCategoryTarget(category: Category, courseIds: List<Long>): List<Course>
}
