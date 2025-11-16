package com.yourssu.soongpt.domain.usaint.application.dto

/**
 * 클라이언트 -> WAS 요청 DTO.
 *
 * - studentId, sToken을 기반으로 u-saint 정보를 동기화합니다.
 * - 응답으로는 timetables가 아닌, 동기화 상태/요약 정보를 제공합니다.
 */
data class UsaintSyncRequest(
    val studentId: String,
    val sToken: String,
)
