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

/**
 * 졸업사정표 핵심 요약 (name 기반 분류 결과). rusaint-service에서 매칭되지 않은 항목은 null.
 * majorRequiredElectiveCombined == true 이면 전필·전선 복합필드, majorRequired == majorElective 동일값, 공통응답에 warning.
 */
data class RusaintGraduationSummaryDto(
    val generalRequired: RusaintCreditSummaryItemDto? = null,
    val generalElective: RusaintCreditSummaryItemDto? = null,
    val majorFoundation: RusaintCreditSummaryItemDto? = null,
    val majorRequired: RusaintCreditSummaryItemDto? = null,
    val majorElective: RusaintCreditSummaryItemDto? = null,
    val majorRequiredElectiveCombined: Boolean? = null,
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
) {
    /** 유세인트 원본 데이터가 None이었을 때 Python _safe_int(None)→0 변환 결과 패턴 */
    fun isEmptyData(): Boolean = required == 0 && completed == 0 && satisfied
}

/** 채플 요건 요약 (학점 없이 충족 여부만) */
data class RusaintChapelSummaryItemDto(
    val satisfied: Boolean,
)
