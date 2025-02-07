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
        return timetableCandidate.isCorrect() && timetableCandidate.isCorrectCreditRule() && timetableCandidate.hasOverlappingCourseTimes()
    }
}
