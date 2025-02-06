package com.yourssu.soongpt.domain.course.implement

class Courses(
    val values: List<Course>,
) {
    fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    fun unpackNameAndProfessor(): List<Pair<String, String>> {
        return values.map { it.courseName to it.professorName!! }
    }

    fun getAllIds(): List<Long> {
        return values.map { it.id!! }
    }
}