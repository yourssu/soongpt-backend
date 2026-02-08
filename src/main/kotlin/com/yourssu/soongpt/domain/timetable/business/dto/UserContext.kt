package com.yourssu.soongpt.domain.timetable.business.dto

data class UserContext(
    val userId: String,
    val departmentName: String,
    val grade: Int,
    val schoolId: Int,
    val division: String
)
