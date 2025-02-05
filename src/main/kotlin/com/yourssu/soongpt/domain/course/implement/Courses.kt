package com.yourssu.soongpt.domain.course.implement

class Courses(
    private val courses: List<Course>,
) {
    fun isEmpty(): Boolean {
        return courses.isEmpty()
    }
}