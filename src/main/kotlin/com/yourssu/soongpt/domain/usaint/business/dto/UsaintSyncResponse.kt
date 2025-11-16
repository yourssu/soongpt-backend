package com.yourssu.soongpt.domain.usaint.business.dto

/**
 * u-saint 동기화 결과 응답.
 *
 * - summary: 동기화 결과에 대한 간단한 설명 (추후 상세 필드로 확장 가능)
 */
data class UsaintSyncResponse(
    val summary: String,
)
