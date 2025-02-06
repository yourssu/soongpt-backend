package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.timetable.implement.CourseTimes

class NoEveningClassesStrategy: TagStrategy {
    override fun isCorrect(courses: Courses, courseTimes: CourseTimes): Boolean {
        return !courseTimes.hasLessClassesEnded(Time.of("18:30"))
    }
}