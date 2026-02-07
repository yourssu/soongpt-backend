package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

data class TimetableCourseResponse(
    val id: Long? = null,
    val category: Category,
    val subCategory: String? = null,
    val field: String? = null,
    val code: Long,
    val name: String,
    val professor: String? = null,
    val department: String,
    val division: String? = null,
    val time: String,
    val point: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
    val courseTimes: List<CourseTimeResponse> = listOf(),
) {
    companion object {
        fun from(
            course: Course,
            courseTimes: List<CourseTime>,
        ): TimetableCourseResponse {
            return TimetableCourseResponse(
                id = course.id,
                category = course.category,
                subCategory = course.subCategory,
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
                courseTimes = courseTimes.map { CourseTimeResponse.from(it) },
            )
        }
    }
}
