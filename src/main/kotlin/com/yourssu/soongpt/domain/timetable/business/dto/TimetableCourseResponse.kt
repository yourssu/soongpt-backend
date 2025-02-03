package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

data class TimetableCourseResponse(
    val courseName: String,
    val professorName: String?,
    val classification: String,
    val courseCode: Int,
    val credit: Int,
    val courseTime: List<CourseTimeResponse>,
) {
    companion object {
        fun from(course: Course, courseTimes: List<CourseTime>): TimetableCourseResponse {
            return TimetableCourseResponse(
                courseName = course.courseName,
                professorName = course.professorName,
                classification = course.classification.name,
                courseCode = course.courseCode,
                credit = course.credit,
                courseTime = courseTimes.map { CourseTimeResponse.from(it) },
            )
        }
    }
}