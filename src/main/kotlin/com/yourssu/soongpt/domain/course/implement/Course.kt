package com.yourssu.soongpt.domain.course.implement

class Course(
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
){
}
