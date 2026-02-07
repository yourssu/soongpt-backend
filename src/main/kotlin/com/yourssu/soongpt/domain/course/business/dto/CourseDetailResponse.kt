package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes

data class CourseDetailResponse(
    val category: String,
    val subCategory: String?,
    val multiMajorCategory: String?,
    val field: String?,
    val code: Long,
    val name: String,
    val professor: String?,
    val department: String,
    val division: String?,
    val time: String,
    val point: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
    val courseTimes: List<CourseTimeResponse>
) {
    companion object {
        fun from(course: Course, courseTimes: CourseTimes): CourseDetailResponse {
            val courseTimeResponses = courseTimes.toList()
                .map { CourseTimeResponse.from(it) }
            return CourseDetailResponse(
                category = course.category.name,
                subCategory = course.subCategory,
                multiMajorCategory = course.multiMajorCategory,
                field = course.field,
                code = course.code,
                name = course.name,
                professor = course.professor,
                department = course.department,
                division = course.division,
                time = course.time,
                point = course.point,
                personeel = course.personeel,
                scheduleRoom = course.scheduleRoom,
                target = course.target,
                courseTimes = courseTimeResponses,
            )
        }
    }
}
