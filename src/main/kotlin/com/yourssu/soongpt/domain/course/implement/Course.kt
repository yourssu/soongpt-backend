package com.yourssu.soongpt.domain.course.implement

class Course(
    val id: Long? = null,
    val category: Category,
    val subCategory: String? = null,
    val field: String? = null,
    val code: Int,
    val name: String,
    val professor: String? = null,
    val department: String,
    val timePoints: String,
    val personeel: Int,
    val scheduleRoom: String,
    val target: String,
){
}
