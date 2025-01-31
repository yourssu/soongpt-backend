package com.yourssu.soongpt.domain.courseTime.business.dto

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

data class CourseTimeResponse(
    val week: String,
    val start: String,
    val end: String,
    val classroom: String?,
) {
    companion object {
        fun from(courseTime: CourseTime): CourseTimeResponse {
            return CourseTimeResponse(
                week = courseTime.week.displayName,
                start = courseTime.startTime.toTimeFormat(),
                end = courseTime.endTime.toTimeFormat(),
                classroom = courseTime.classroom
            )
        }
    }
}
