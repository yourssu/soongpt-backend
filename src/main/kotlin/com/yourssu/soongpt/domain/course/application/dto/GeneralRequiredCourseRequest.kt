package com.yourssu.soongpt.domain.course.application.dto

import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class GeneralRequiredCourseRequest(
    @Range(min = 15, max = 25, message = "학번은 15부터 25까지 가능합니다.")
    val schoolId: Long,

    @NotBlank
    val department: String,

    @Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,
) {
}