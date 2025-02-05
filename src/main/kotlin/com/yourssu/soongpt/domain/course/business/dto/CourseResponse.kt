package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

data class CourseResponse(
    val courseName: String,
    val professorName: String?,
    val classification: String,
    val credit: Int,
    val target: List<String>,
    val courseTime: List<CourseTimeResponse>,
) {
    companion object {
        fun from(course: Course, target: List<String>, courseTimes: List<CourseTime>): CourseResponse {
            return CourseResponse(
                courseName = course.courseName,
                professorName = course.professorName,
                classification = course.classification.name,
                credit = course.credit,
                target = target,
                courseTime = courseTimes.map { CourseTimeResponse.from(it) },
            )
        }
    }
}
