package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week
import com.yourssu.soongpt.domain.timetable.implement.strategy.FreeDayTagStrategy

class CourseTimes(
    private val values: List<CourseTime>
) {
    fun extend(courseTimes: CourseTimes): CourseTimes {
        return CourseTimes(this.values + courseTimes.values)
    }

    fun hasOverlappingCourseTimes(): Boolean {
        for (i in values.indices) {
            for (j in i + 1 until values.size) {
                if (values[i].week == values[j].week &&
                    values[j].endTime.isOverThan(values[i].startTime) &&
                    values[i].endTime.isOverThan(values[j].startTime)
                ) {
                    return true
                }
            }
        }
        return false
    }

    fun hasFreeDay(): Boolean {
        for (day in Week.weekdays()) {
            if (values.none { it.week == day }) {
                return true
            }
        }
        return false
    }

    fun hasOverClassesStarted(standard: Time): Boolean {
        return values.any { standard.isOverThan(it.startTime) }
    }

    fun hasLessClassesEnded(standard: Time): Boolean {
        return values.any { it.endTime.isOverThan(standard) }
    }

    fun hasBreaks(minute: Int): Boolean {
        for (i in values.indices) {
            for (j in i + 1 until values.size) {
                if (values[i].week != values[j].week) {
                    continue
                }
                if (isOverlapping(i, j, minute)) {
                    return true
                }
            }
        }
        return false
    }

    fun hasContinuousTime(startTimeLimit: Time, endTimeLimit: Time, requiredMinutes: Int): Boolean {
        for (day in Week.weekdays()) {
            val classesToday = values.filter { it.week == day }
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

    fun countMorningClasses(): Int {
        return values.count { Time.getMorningTime().isOverThan(it.startTime) }
    }

    fun countEveningClasses(): Int {
        return values.count { Time.getMorningTime().isOverThan(it.startTime) }
    }

    fun countOneClassPerDay(): Int {
        return Week.weekdays().count { day -> values.count { it.week == day } == 1 }
    }

    fun countFreeDayScore(): Int {
        return Week.weekdays()
            .filter { values.none() }
            .sumOf { FreeDayTagStrategy.getFreeDayScore(it) }
    }

    fun countBreaks(minute: Int): Int {
        var score = 0
        for (i in values.indices) {
            for (j in i + 1 until values.size) {
                if (values[i].week != values[j].week) {
                    continue
                }
                if (isOverlapping(i, j, minute)) {
                    score += 1
                }
            }
        }
        return score
    }

    private fun isOverlapping(i: Int, j: Int, minute: Int): Boolean {
        return ((values[j].startTime.isOverThan(values[i].endTime)) &&
                !values[i].endTime.addMinute(minute).isOverThan(values[j].startTime) ||
                (values[i].startTime.isOverThan(values[j].endTime) &&
                        !values[j].endTime.addMinute(minute).isOverThan(values[i].startTime)))
    }
}
