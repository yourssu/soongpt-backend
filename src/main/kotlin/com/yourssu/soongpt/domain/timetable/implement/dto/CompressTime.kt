package com.yourssu.soongpt.domain.timetable.implement.dto

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.timetable.implement.timeslot.TIMESLOT_UNIT_MINUTES

data class CompressTime(
    val compressedStartTime: Int,
    val compressedEndTime: Int,
) {
    companion object {
        fun from(courseTime: CourseTime): CompressTime {
            val start = courseTime.startTime.time / TIMESLOT_UNIT_MINUTES
            val end = courseTime.endTime.time / TIMESLOT_UNIT_MINUTES
            return CompressTime(start, end)
        }
    }
}
