package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import org.springframework.stereotype.Component

@Component
class LabTimetableValidator {
    fun filterValidCourses(courses: List<Course>): List<Course> {
        return courses.filter { course ->
            val credit = course.time.toDoubleOrNull()?.toInt() ?: 0
            val times = CourseTimes.from(course.scheduleRoom).toList()

            // credit > 0, 시간이 존재, 과목명이 비어있지 않음
            val hasValidCredit = credit > 0
            val hasValidTimes = times.isNotEmpty()
            val hasValidName = course.name.isNotBlank()

            // 모든 필수 필드가 유효한지 확인
            hasValidCredit && hasValidTimes && hasValidName
        }
    }

    fun hasOverlap(courses: List<Course>): Boolean {
        data class Slot(val week: String, val start: Int, val end: Int)
        val slots = courses.flatMap { course ->
            CourseTimes.from(course.scheduleRoom).toList().map {
                Slot(
                    week = it.week.displayName,
                    start = it.startTime.time,
                    end = it.endTime.time,
                )
            }
        }
        val grouped = slots.groupBy { it.week }
        for ((_, daySlots) in grouped) {
            val sorted = daySlots.sortedBy { it.start }
            for (i in 1 until sorted.size) {
                val prev = sorted[i - 1]
                val cur = sorted[i]
                if (prev.end > cur.start) return true
            }
        }
        return false
    }
}
