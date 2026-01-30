package com.yourssu.soongpt.domain.usaint.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

/**
 * 클라이언트 -> WAS 요청 DTO.
 *
 * - studentId, sToken을 기반으로 u-saint 정보를 동기화합니다.
 * - 응답으로는 timetables가 아닌, 동기화 상태/요약 정보를 제공합니다.
 */
data class UsaintSyncRequest(
    @field:NotBlank
    @field:Pattern(regexp = "^20(1[5-9]|2[0-5])\\d{4}$", message = "학번 형식이 올바르지 않습니다")
    val studentId: String,

    @field:NotBlank
    val sToken: String,
)
