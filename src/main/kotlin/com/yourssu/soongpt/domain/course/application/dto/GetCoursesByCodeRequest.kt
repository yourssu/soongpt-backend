package com.yourssu.soongpt.domain.course.application.dto

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class GetCoursesByCodeRequest(
    @field:NotNull(message = "강의 코드는 필수입니다")
    @field:NotEmpty(message = "강의 코드는 비어있을 수 없습니다")
    val code: List<Long>
)