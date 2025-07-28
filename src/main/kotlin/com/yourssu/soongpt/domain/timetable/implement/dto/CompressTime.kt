package com.yourssu.soongpt.domain.timetable.implement.dto

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_DAY_RANGE
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_UNIT_MINUTES

data class CompressTime(
    val compressedStartTime: Int,
    val compressedEndTime: Int,
) {
    companion object {
        fun from(courseTime: CourseTime): CompressTime {

            if (courseTime.week == Week.UNKNOWN) {
                return CompressTime(
                    compressedStartTime = 0,
                    compressedEndTime = 0,
                )
            } else {
                val day = courseTime.week.ordinal * TIMESLOT_DAY_RANGE
                val start = day + courseTime.startTime.time / TIMESLOT_UNIT_MINUTES
                val end = day + courseTime.endTime.time / TIMESLOT_UNIT_MINUTES
                return CompressTime(start, end)
            }
        }

        fun from(startTime: Time, endTime: Time): CompressTime {
            return CompressTime(
                compressedStartTime = startTime.time / TIMESLOT_UNIT_MINUTES,
                compressedEndTime = endTime.time / TIMESLOT_UNIT_MINUTES
            )
        }
    }
}
