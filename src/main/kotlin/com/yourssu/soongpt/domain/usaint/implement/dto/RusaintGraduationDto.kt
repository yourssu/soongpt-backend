package com.yourssu.soongpt.domain.usaint.implement.dto

/** rusaint-service `/snapshot/graduation` 응답. */
data class RusaintGraduationResponseDto(
    val pseudonym: String,
    val graduationRequirements: RusaintGraduationRequirementsDto,
)

data class RusaintRemainingCreditsDto(
    val majorRequired: Int,
    val majorElective: Int,
    val generalRequired: Int,
    val generalElective: Int,
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
    val remainingCredits: RusaintRemainingCreditsDto,
)
