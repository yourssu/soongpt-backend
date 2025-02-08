package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses

class TimetableCandidates(
    val values: List<TimetableCandidate>
) {
    fun filterRules(): TimetableCandidates {
        return TimetableCandidates(values.filter { timetableRules(it) })
    }

    fun extendCourses(addCourses: List<Triple<Courses, CourseTimes, Int>>): TimetableCandidates {
        return TimetableCandidates(addCourses.flatMap { (courses, courseTimes, score) ->
            values.map {
                it.generateNewTimetableCandidate(courses, courseTimes, score)
            }
        } + values)
    }

    private fun timetableRules(timetableCandidate: TimetableCandidate): Boolean {
        val isCorrect = timetableCandidate.isCorrect()
        val isCorrectCreditRule = timetableCandidate.isCorrectCreditRule()
        val hasOverlappingCourseTimes = timetableCandidate.hasOverlappingCourseTimes()
        return isCorrect && isCorrectCreditRule && !hasOverlappingCourseTimes
    }

    fun pickTopNOfFinalScores(n: Int, maximumTagLimit: Int): TimetableCandidates {
        return TimetableCandidates(values.groupBy { it.tag }
            .map { timetables -> timetables.value
                .sortedByDescending { it.calculateFinalScore() }
                .take(maximumTagLimit)
            }.flatten()
            .take(n))
    }
}
