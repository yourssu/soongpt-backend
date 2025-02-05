package com.yourssu.soongpt.domain.timetable.implement.strategy

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.timetable.implement.CourseTimes

interface TagStrategy {
    fun isCorrect(courses: Courses, courseTimes: CourseTimes): Boolean
}
