package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.timetable.implement.dto.CourseCandidate
import org.springframework.stereotype.Component

@Component
class LabTimetableValidator(
    private val courseCandidateFactory: CourseCandidateFactory,
) {
    fun filterValidCourses(courses: List<Course>): List<Course> {
        return courses.filter { course ->
            val credit = course.time.toDoubleOrNull()?.toInt() ?: 0
            val times = CourseTimes.from(course.scheduleRoom).toList()

            val hasValidCredit = credit > 0
            val hasValidTimes = times.isNotEmpty()
            val hasValidName = course.name.isNotBlank()

            hasValidCredit && hasValidTimes && hasValidName
        }
    }

    fun hasOverlap(courses: List<Course>): Boolean {
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

    fun hasChapel(courses: List<Course>): Boolean {
        return courses.any { it.category == Category.CHAPEL }
    }
}
