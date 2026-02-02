package com.yourssu.soongpt.domain.usaint.implement.dto

/** rusaint-service `/snapshot/academic` 응답 (졸업사정표 제외). PSEUDONYM_SECRET 미설정 시 서버가 기동하지 않으므로 pseudonym은 항상 존재. */
data class RusaintAcademicResponseDto(
    val pseudonym: String,
    val takenCourses: List<RusaintTakenCourseDto>,
    val lowGradeSubjectCodes: RusaintLowGradeSubjectCodesDto,
    val flags: RusaintStudentFlagsDto,
    val availableCredits: RusaintAvailableCreditsDto,
    val basicInfo: RusaintBasicInfoDto,
)

data class RusaintTakenCourseDto(
    val year: Int,
    val semester: String,
    val subjectCodes: List<String>,
)

data class RusaintLowGradeSubjectCodesDto(
    val passLow: List<String>,
    val fail: List<String>,
)

data class RusaintStudentFlagsDto(
    val doubleMajorDepartment: String?,
    val minorDepartment: String?,
    val teaching: Boolean,
)

data class RusaintAvailableCreditsDto(
    val previousGpa: Double,
    val carriedOverCredits: Int,
    val maxAvailableCredits: Double,
)

data class RusaintBasicInfoDto(
    val year: Int,
    val semester: Int,
    val grade: Int,
    val department: String,
)
