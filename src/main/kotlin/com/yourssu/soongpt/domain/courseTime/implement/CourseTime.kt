package com.yourssu.soongpt.domain.courseTime.implement

import com.yourssu.soongpt.domain.courseTime.implement.exception.InvalidCourseTimeException

class CourseTime(
    val id: Long? = null,
    val week: Week,
    val startTime: Time,
    val endTime: Time,
    val classroom: String? = null,
) {
    init {
        if (startTime.isOverThan(endTime)) {
            throw InvalidCourseTimeException()
        }
    }
}
