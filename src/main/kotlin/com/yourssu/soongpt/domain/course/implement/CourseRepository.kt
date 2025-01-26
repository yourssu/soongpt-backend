package com.yourssu.soongpt.domain.course.implement

interface CourseRepository {
    fun save(course: Course): Course
    fun get(id: Long): Course
}