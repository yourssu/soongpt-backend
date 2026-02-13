package com.yourssu.soongpt.domain.usaint.implement.dto

/** rusaint-service `/snapshot/graduation` 응답. */
data class RusaintGraduationResponseDto(
    val pseudonym: String,
    val graduationRequirements: RusaintGraduationRequirementsDto,
    val graduationSummary: RusaintGraduationSummaryDto,
)

data class RusaintGraduationRequirementItemDto(
    val name: String,
    val requirement: Int?,
    val calculation: Double?,
    val difference: Double?,
    val result: Boolean,
    val category: String,
)

data class RusaintGraduationRequirementsDto(
    val requirements: List<RusaintGraduationRequirementItemDto>,
)

/** 졸업사정표 핵심 요약 (name 기반 분류 결과). rusaint-service에서 매칭되지 않은 항목은 null. */
data class RusaintGraduationSummaryDto(
    val generalRequired: RusaintCreditSummaryItemDto? = null,
    val generalElective: RusaintCreditSummaryItemDto? = null,
    val majorFoundation: RusaintCreditSummaryItemDto? = null,
    val majorRequired: RusaintCreditSummaryItemDto? = null,
    val majorElective: RusaintCreditSummaryItemDto? = null,
    val minor: RusaintCreditSummaryItemDto? = null,
    val doubleMajorRequired: RusaintCreditSummaryItemDto? = null,
    val doubleMajorElective: RusaintCreditSummaryItemDto? = null,
    val christianCourses: RusaintCreditSummaryItemDto? = null,
    val chapel: RusaintChapelSummaryItemDto? = null,
)

/** 학점 기반 졸업 요건 요약 항목 */
data class RusaintCreditSummaryItemDto(
    val required: Int,
    val completed: Int,
    val satisfied: Boolean,
)

/** 채플 요건 요약 (학점 없이 충족 여부만) */
data class RusaintChapelSummaryItemDto(
    val satisfied: Boolean,
)
