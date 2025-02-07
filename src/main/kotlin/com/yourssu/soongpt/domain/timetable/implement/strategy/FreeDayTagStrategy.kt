package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.CourseTimes

class FreeDayTagStrategy : TagStrategy {
    companion object {
        const val ONE_CLASS_PER_DAY_SCORE = -11
        private val freeDayScores = mapOf(
            Week.MONDAY to 25,
            Week.WEDNESDAY to 15,
            Week.FRIDAY to 30,
        )

        fun getFreeDayScore(week: Week): Int {
            return freeDayScores[week] ?: 0
        }
    }

    override fun isCorrect(courses: Courses, courseTimes: CourseTimes): Boolean {
        return courseTimes.hasFreeDay()
    }
}
