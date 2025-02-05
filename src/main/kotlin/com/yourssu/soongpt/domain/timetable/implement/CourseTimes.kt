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
        for (i in courseTimes.indices) {
            for (j in i + 1 until courseTimes.size) {
                if (courseTimes[i].week != courseTimes[j].week) {
                    continue
                }
                if ((courseTimes[j].startTime.isOverThan(courseTimes[i].endTime)) &&
                    courseTimes[i].endTime.addMinute(minute).isOverThan(courseTimes[j].startTime)
                ) {
                    return true
                }
                if (courseTimes[i].startTime.isOverThan(courseTimes[j].endTime) &&
                    courseTimes[j].endTime.addMinute(minute).isOverThan(courseTimes[i].startTime)
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun hasContinuousTime(startTimeLimit: Time, endTimeLimit: Time, requiredMinutes: Int): Boolean {
        for (day in Week.weekdays()) {
            val classesToday = courseTimes.filter { it.week == day }
            if (classesToday.isEmpty()) {
                continue
            }
            var lastEndTime = startTimeLimit
            for (course in classesToday.sortedBy { it.startTime.time }) {
                if (course.startTime.isOverThan(lastEndTime) && course.startTime.minus(lastEndTime) >= requiredMinutes) {
                    break
                }
                lastEndTime = course.endTime
            }
            if (endTimeLimit.isOverThan(lastEndTime) && (endTimeLimit.minus(lastEndTime) >= requiredMinutes)) {
                break
            }
            return false
        }
        return true
    }
}
