package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.timetable.implement.CourseTimes

class NoLongBreaksStrategy: TagStrategy {
    companion object {
        const val BREAKS_MINUTE = 60
        const val BREAKS_SCORE = -7
    }

    override fun isCorrect(courses: Courses, courseTimes: CourseTimes): Boolean {
        return !courseTimes.hasBreaks(BREAKS_MINUTE)
    }
}