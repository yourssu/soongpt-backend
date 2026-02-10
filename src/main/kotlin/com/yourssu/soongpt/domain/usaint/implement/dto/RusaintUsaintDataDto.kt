package com.yourssu.soongpt.domain.usaint.implement.dto

/** academic + graduation 병합 스냅샷. */
data class RusaintUsaintDataResponse(
    val pseudonym: String,
    val takenCourses: List<RusaintTakenCourseDto>,
    val lowGradeSubjectCodes: List<String>,
    val flags: RusaintStudentFlagsDto,
    val basicInfo: RusaintBasicInfoDto,
    val graduationRequirements: RusaintGraduationRequirementsDto? = null,
    val graduationSummary: RusaintGraduationSummaryDto? = null,
    val warnings: List<String> = emptyList(),
)
