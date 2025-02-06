package com.yourssu.soongpt.domain.timetable.implement

class TimetableCandidates(
    val values: List<TimetableCandidate>
) {
    fun filterTagStrategy(): TimetableCandidates {
        return TimetableCandidates(values.filter { it.isCorrect() })
    }
}