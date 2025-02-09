package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Courses

class TimetableCandidates(
    val values: List<TimetableCandidate>
) {
    companion object {
        const val TAG_CANDIDATES_SIZE = 5
    }

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
        val result = TimetableCandidates(
            values.groupBy { it.tag }
                .asSequence()
                .filter { it.key != Tag.DEFAULT }
                .map { timetables ->
                    timetables.value
                        .sortedByDescending { it.calculateFinalScore() }
                        .take(maximumTagLimit)
                }.flatten()
                .sortedByDescending { it.calculateFinalScore() }
                .take(n)
                .toList())
        if (validateMinimumTimetablePolicy(result)) {
            return TimetableCandidates(result.values + values.filter { it.tag == Tag.DEFAULT }
                .take(TAG_CANDIDATES_SIZE - result.values.size))
        }
        return result
    }

    private fun validateMinimumTimetablePolicy(result: TimetableCandidates) =
        result.values.size < TAG_CANDIDATES_SIZE
}
