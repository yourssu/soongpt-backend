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

    fun allCasesLessThan(standard: Int): List<Courses> {
        val combinations = mutableListOf<List<Course>>(emptyList())
        for (courses in values) {
            val newCombinations = mutableListOf<List<Course>>()
            for (currentCombination in combinations) {
                for (course in courses.values) {
                    if (currentCombination.sumOf { it.credit } + course.credit > standard) {
                        continue
                    }
                    newCombinations.add(currentCombination + course)
                }
            }
            combinations.addAll(newCombinations)
        }

        return combinations.map { Courses(it) }
    }

    fun filterLessThanTotalCredit(standard: Int): List<Courses> {
        return values.filter { it.totalCredit() < standard }
    }

    fun sortByRatingAverage(ratings: Map<Long, Double>): List<Courses> {
        return values.sortedWith(
            compareBy(
                { courses -> -courses.totalCredit() },
                { courses -> -(courses.values.sumOf { course -> ratings[course.id]!! } / courses.values.size) },
                { courses -> -courses.values.size }
            )
        )
    }
}
