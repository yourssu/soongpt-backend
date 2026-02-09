package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category

data class CreateCourseCommand(
    val category: Category,
    val subCategory: String?,
    val multiMajorCategory: String? = null,
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
    val target: String
)
