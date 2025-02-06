package com.yourssu.soongpt.domain.course.implement

class Courses(
    val values: List<Course>,
) {
    fun isEmpty(): Boolean {
        return values.isEmpty()
    }

    fun totalCredit(): Int {
        return values.sumOf { it.credit }
    }

    fun getFirstCredit(): Int {
        if (values.isEmpty()) {
            return 0
        }
        return values.first().credit
    }

    fun unpackNameAndProfessor(): List<Pair<String, String>> {
        return values.map { it.courseName to it.professorName!! }
    }

    fun getAllIds(): List<Long> {
        return values.map { it.id!! }
    }

    fun groupByCourseNames(): List<Courses> {
        return values.groupBy { it.courseName }
            .map { Courses(it.value) }
    }

    fun add(course: Course): Courses {
        return Courses(this.values + course)
    }

    fun extend(courses: Courses): Courses {
        return Courses(this.values + courses.values)
    }
}