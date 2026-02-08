package com.yourssu.soongpt.domain.sso.application.dto

data class SyncStatusResponse(
    val status: String,
    val reason: String? = null,
    val studentInfo: StudentInfoResponse? = null,
)

data class StudentInfoResponse(
    val grade: Int,
    val semester: Int,
    val year: Int,
    val department: String,
    val doubleMajorDepartment: String?,
    val minorDepartment: String?,
    val teaching: Boolean,
)

data class StudentInfoUpdateRequest(
    val grade: Int,
    val semester: Int,
    val year: Int,
    val department: String,
    val doubleMajorDepartment: String?,
    val minorDepartment: String?,
    val teaching: Boolean,
)
