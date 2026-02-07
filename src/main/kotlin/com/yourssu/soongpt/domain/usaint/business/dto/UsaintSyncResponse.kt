package com.yourssu.soongpt.domain.usaint.business.dto

/**
 * u-saint 동기화 결과 응답.
 *
 * - summary: 동기화 결과에 대한 간단한 설명
 * - pseudonym: 학번 기반 익명 식별자. 클라이언트는 저장 후 GET /api/usaint/retake 등에서 X-Pseudonym 헤더로만 전달
 */
data class UsaintSyncResponse(
    val summary: String,
    val pseudonym: String,
)
