package com.yourssu.soongpt.domain.course.implement

import kotlin.random.Random

class CoursesFactory(
    private val values: List<Courses>,
) {
    companion object {
        private const val MAX_RATING = 70.0
    }

    fun generateTimetableCandidates(): Pair<List<Courses>, Courses> {
        val timetableCandidates = values.fold(listOf(emptyList<Course>())) { acc, courses ->
            acc.flatMap { currentCombination ->
                courses.values.map { course -> currentCombination + course }
            }
        }.map { Courses(it) }
        val courses = Courses(values.flatMap { it.values })
        return Pair(timetableCandidates, courses)
    }

    fun districtDuplicatedCourses(target: List<Courses>): CoursesFactory {
        if (target.isEmpty()) {
            return this
        }
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

    fun allCasesLessThan(standard: Int, taken: Int): List<Courses> {
        val combinations = mutableListOf<List<Course>>(emptyList())
        for (courses in values) {
            if (combinations.size >= taken) {
                break
            }
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

    fun sortByRatingAverage(ratings: Map<Long, Double>): List<Pair<Courses, Double>> {
        return values.sortedWith(
            compareBy(
                { courses -> -courses.totalCredit() },
                { courses -> -(courses.values.sumOf { course -> ratings[course.id]!!.coerceAtMost(MAX_RATING) } / courses.values.size) },
                { Random.nextInt(10) },
                { courses -> -courses.values.size })
        )
            .map {
                Pair(it, it.values.sumOf { course -> ratings[course.id]!!.coerceAtMost(MAX_RATING) } / it.values.size)
            }
    }
}
