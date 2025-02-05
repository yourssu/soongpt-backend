package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week

class CourseTimes(
    private val courseTimes: List<CourseTime>
) {
    fun hasOverlappingCourseTimes(): Boolean {
        for (i in courseTimes.indices) {
            for (j in i + 1 until courseTimes.size) {
                if (courseTimes[i].week == courseTimes[j].week &&
                    courseTimes[j].endTime.isOverThan(courseTimes[i].startTime) &&
                    courseTimes[i].endTime.isOverThan(courseTimes[j].startTime)
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun hasFreeDay(): Boolean {
        for (day in Week.weekdays()) {
            if (courseTimes.none { it.week == day }) {
                return true
            }
        }
        return false
    }

    fun hasOverClasses(standard: Time): Boolean {
        return courseTimes.any { standard.isOverThan(it.startTime) }
    }

    fun hasBreaks(minute: Int): Boolean {
        TODO("Not yet implemented")
    }
}
