package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.department.implement.Department

data class UserContext(
    val userId: String,
    val department: Department,
    val grade: Int,
    val schoolId: Int,
    val division: String
)
