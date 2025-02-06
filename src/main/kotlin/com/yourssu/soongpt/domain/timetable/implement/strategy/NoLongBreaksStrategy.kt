package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.timetable.implement.CourseTimes

class NoLongBreaksStrategy: TagStrategy {
    override fun isCorrect(courses: Courses, courseTimes: CourseTimes): Boolean {
        return !courseTimes.hasBreaks(150)
    }
}