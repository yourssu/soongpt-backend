package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.InvalidTimetableRequestException

class CoursesFactory(
    private val values: List<Courses>,
) {
    fun generateTimetableCandidates(): List<Courses> {
        val timetableCandidates = values.fold(listOf(emptyList<Course>())) { acc, courses ->
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

    fun districtDuplicatedCourses(target: List<Courses>): CoursesFactory {
        return CoursesFactory(values.filter { courses ->
            target.map { it.unpackNameAndProfessor().first().first != courses.unpackNameAndProfessor().first().first }
                .reduce(Boolean::and)
        })
    }

    fun allCases(): CoursesFactory {
        val combinations = mutableListOf<List<Course>>(emptyList())

        for (courses in values) {
            val newCombinations = mutableListOf<List<Course>>()
            for (currentCombination in combinations) {
                for (course in courses.values) {
                    newCombinations.add(currentCombination + course)
                }
            }
            combinations.addAll(newCombinations)
        }

        return CoursesFactory(combinations.map { Courses(it) })
    }



    fun filterLessThanTotalCredit(standard: Int): List<Courses> {
        return values.filter { it.totalCredit() < standard }
    }
}
