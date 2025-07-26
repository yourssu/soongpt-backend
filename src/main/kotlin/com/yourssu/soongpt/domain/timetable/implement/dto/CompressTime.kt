package com.yourssu.soongpt.domain.timetable.implement.dto

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

private const val COMPRESS_TIME_UNIT = 5

data class CompressTime(
    val compressedStartTime: Int,
    val compressedEndTime: Int,
) {
    companion object {
        fun from(courseTime: CourseTime): CompressTime {
            val start = courseTime.startTime.time / COMPRESS_TIME_UNIT
            val end = courseTime.endTime.time / COMPRESS_TIME_UNIT
            return CompressTime(start, end)
        }
    }
}
