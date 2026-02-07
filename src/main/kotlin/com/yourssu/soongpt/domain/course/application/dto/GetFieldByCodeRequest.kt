package com.yourssu.soongpt.domain.course.application.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class GetFieldByCodeRequest(
    @field:NotNull(message = "강의 코드는 필수입니다")
    @field:NotEmpty(message = "강의 코드는 비어있을 수 없습니다")
    val code: List<Long>,

    @field:NotNull(message = "학번은 필수입니다")
    val schoolId: Int
)
