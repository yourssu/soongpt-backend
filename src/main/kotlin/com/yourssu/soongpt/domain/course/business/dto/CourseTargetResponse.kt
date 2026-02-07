package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType

data class CourseTargetResponse(
    val code: Long,
    val name: String,
    val professor: String?,
    val category: Category,
    val subCategory: String?,
    val multiMajorCategory: String?,
    val department: String,
    val division: String?,
    val point: String,
    val time: String,
    val personeel: Int,
    val scheduleRoom: String,
    val targetText: String,
    val field: String?,
    val courseTimes: List<CourseTimeResponse>,
    val targets: List<TargetInfo>
)

data class TargetInfo(
    val id: Long?,
    val scopeType: ScopeType,
    val scopeId: Long?,
    val scopeName: String?,
    val grade1: Boolean,
    val grade2: Boolean,
    val grade3: Boolean,
    val grade4: Boolean,
    val grade5: Boolean,
    val studentType: StudentType,
    val isStrict: Boolean,
    val isDenied: Boolean
)
