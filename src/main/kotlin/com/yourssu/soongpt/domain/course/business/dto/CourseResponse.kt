package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

data class CourseResponse(
    val id: Long? = null,
    val category: Category,
    val subCategory: String? = null,
    val field: String? = null,
    val code: Long,
    val name: String,
    val professor: String? = null,
    val department: String,
    val timePoints: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
    val courseTimes: List<CourseTime> = listOf(),
) {
    companion object {
        fun from(
            course: Course,
            courseTimes: List<CourseTime>,
        ): CourseResponse {
            return CourseResponse(
                id = course.id,
                category = course.category,
                subCategory = course.subCategory,
                field = course.field,
                code = course.code,
                name = course.name,
                professor = course.professor,
                department = course.department,
                timePoints = course.timePoints,
                personeel = course.personeel,
                scheduleRoom = course.scheduleRoom,
                target = course.target,
                courseTimes = courseTimes,
            )
        }
    }
}
