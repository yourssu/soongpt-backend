package com.yourssu.soongpt.domain.course.implement

import org.springframework.stereotype.Component

@Component
class CoursesFactory {
    fun generateTimetableCandidates(
        coursesCandidates: List<Courses>
    ): List<Courses> {
        return coursesCandidates.fold(listOf(emptyList<Course>())) { acc, courses ->
            acc.flatMap { currentCombination ->
                courses.courses.map { course -> currentCombination + course }
            }
        }.map { Courses(it) }
    }
}
