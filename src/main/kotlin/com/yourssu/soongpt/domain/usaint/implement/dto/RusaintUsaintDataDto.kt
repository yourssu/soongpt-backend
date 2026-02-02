package com.yourssu.soongpt.domain.usaint.implement.dto

/** academic + graduation 병합 스냅샷. */
data class RusaintUsaintDataResponse(
    val pseudonym: String,
    val takenCourses: List<RusaintTakenCourseDto>,
    val lowGradeSubjectCodes: RusaintLowGradeSubjectCodesDto,
    val flags: RusaintStudentFlagsDto,
    val availableCredits: RusaintAvailableCreditsDto,
    val basicInfo: RusaintBasicInfoDto,
    val remainingCredits: RusaintRemainingCreditsDto,
    val graduationRequirements: RusaintGraduationRequirementsDto? = null,
)
