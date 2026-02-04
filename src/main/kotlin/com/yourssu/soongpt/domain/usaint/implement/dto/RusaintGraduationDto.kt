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

/** 졸업사정표 핵심 요약 (name 기반 분류 결과) */
data class RusaintGraduationSummaryDto(
    val generalRequired: RusaintCreditSummaryItemDto,
    val generalElective: RusaintCreditSummaryItemDto,
    val majorFoundation: RusaintCreditSummaryItemDto,
    val majorRequired: RusaintCreditSummaryItemDto,
    val majorElective: RusaintCreditSummaryItemDto,
    val doubleMajorRequired: RusaintCreditSummaryItemDto,
    val doubleMajorElective: RusaintCreditSummaryItemDto,
    val christianCourses: RusaintCreditSummaryItemDto,
    val chapel: RusaintChapelSummaryItemDto,
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
