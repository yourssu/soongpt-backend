package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

data class TimetableCourseResponse(
    val courseName: String,
    val professorName: String?,
    val classification: String,
    val credit: Double,
    val courseTime: List<CourseTimeResponse>,
) {
    companion object {
        fun from(course: Course, courseTimes: List<CourseTime>): TimetableCourseResponse {
            return TimetableCourseResponse(
                courseName = course.courseName,
                professorName = course.professorName,
                classification = course.classification.name,
                credit = course.credit.toDouble() + addCreditIfChapel(course.classification),
                courseTime = courseTimes.map { CourseTimeResponse.from(it) },
            )
        }

        private fun addCreditIfChapel(classification: Classification): Double {
            if (classification == Classification.CHAPEL) {
                return 0.5
            }
            return 0.0
        }
    }
}
