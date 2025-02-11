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

    fun pickTopNOfFinalScores(maximumTagLimit: Int, maximumPerTag: Int, total: Int): TimetableCandidates {
        val candidates = values.filter { it.tag != Tag.DEFAULT }
            .sortedWith(
                compareBy(
                    { -it.calculateFinalScore() },
                    { it.tag.ordinal }
                )
            )
            .toList()
        val timetablesByTag = pickByTags(
            maximumTagLimit = maximumTagLimit,
            maximumPerTag = maximumPerTag,
            candidates = candidates
        )
        val result = pickByScore(total, timetablesByTag, candidates)
        return TimetableCandidates(result)
    }

    private fun pickByTags(
        maximumTagLimit: Int,
        maximumPerTag: Int,
        candidates: List<TimetableCandidate>
    ): List<TimetableCandidate> {
        val result = ArrayList<TimetableCandidate>()
        for (candidate in candidates) {
            if (result.size >= maximumTagLimit) {
                break
            }
            if (result.count { it.tag == candidate.tag } < maximumPerTag &&
                result.none { it.calculateFinalScore() == candidate.calculateFinalScore() }
            ) {
                result.add(candidate)
            }
        }
        return result
    }

    private fun pickByScore(
        maximum: Int,
        preResult: List<TimetableCandidate>,
        candidates: List<TimetableCandidate>
    ): List<TimetableCandidate> {
        val result = ArrayList<TimetableCandidate>(preResult)
        for (candidate in candidates) {
            if (result.size >= maximum) {
                break
            }
            if (result.none { it.calculateFinalScore() == candidate.calculateFinalScore() }) {
                result.add(candidate.copy(tag = Tag.DEFAULT))
            }
        }
        return result
    }

    private fun validateMinimumTimetablePolicy(result: TimetableCandidates, maximumTagLimit: Int) =
        result.values.size < maximumTagLimit
}
