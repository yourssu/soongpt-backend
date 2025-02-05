package com.yourssu.soongpt.domain.course.implement

class Courses(
    val courses: List<Course>,
) {
    fun isEmpty(): Boolean {
        return courses.isEmpty()
    }

    fun unpackNameAndProfessor(): List<Pair<String, String>> {
        return courses.map { it.courseName to it.professorName!! }
    }
}