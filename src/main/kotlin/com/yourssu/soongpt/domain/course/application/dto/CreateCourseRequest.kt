package com.yourssu.soongpt.domain.course.application.dto

data class CreateCourseRequest(
    val syllabus: String?,
    val category: String?,
    val sub_category: String?,
    val abeek_info: String?,
    val field: String?,
    val code: String?,
    val name: String,
    val division: String?,
    val professor: String?,
    val department: String?,
    val time_points: String?,
    val personeel: String?,
    val remaining_seats: String?,
    val schedule_room: String?,
    val target: String?
)
