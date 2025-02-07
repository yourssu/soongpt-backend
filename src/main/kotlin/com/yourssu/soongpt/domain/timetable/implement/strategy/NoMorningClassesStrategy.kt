package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.timetable.implement.CourseTimes

class NoMorningClassesStrategy : TagStrategy {
    companion object {
        const val MORNING_CLASSES_SCORE = -10
    }

    override fun isCorrect(courses: Courses, courseTimes: CourseTimes): Boolean {
        return !courseTimes.hasOverClassesStarted(Time.getMorningTime())
    }
}