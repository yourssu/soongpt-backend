package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.InvalidTimetableRequestException

class CoursesFactory(
    private val coursesCandidates: List<Courses>,
) {
    fun generateTimetableCandidates(): List<Courses> {
        val timetableCandidates = coursesCandidates.fold(listOf(emptyList<Course>())) { acc, courses ->
            acc.flatMap { currentCombination ->
                courses.values.map { course -> currentCombination + course }
            }
        }.map { Courses(it) }
        validateEmpty(timetableCandidates)
        return timetableCandidates
    }

    private fun validateEmpty(timetableCandidates: List<Courses>) {
        if (timetableCandidates.isEmpty()) {
            throw InvalidTimetableRequestException()
        }
    }
}
