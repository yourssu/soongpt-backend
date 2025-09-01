package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.implement.constant.TIMESLOT_SIZE
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.springframework.stereotype.Component
import java.util.*

@Component
class LabTimetableValidator(
    private val courseCandidateFactory: CourseCandidateFactory,
) {
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
        // 기존 TimetableCandidateBuilder의 intersects 로직 활용
        val candidates = courses.map { courseCandidateFactory.create(it) }

        for (i in candidates.indices) {
            for (j in i + 1 until candidates.size) {
                val candidate1 = candidates[i]
                val candidate2 = candidates[j]
                if (candidate1.timeSlot.intersects(candidate2.timeSlot)) {
                    return true
                }
            }
        }
        return false
    }
}
