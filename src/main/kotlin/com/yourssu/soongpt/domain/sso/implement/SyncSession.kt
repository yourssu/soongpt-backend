package com.yourssu.soongpt.domain.sso.implement

import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import java.time.Instant

/**
 * SSO 콜백 후 rusaint 데이터 동기화 세션.
 */
data class SyncSession(
    val pseudonym: String,
    val status: SyncStatus,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val usaintData: RusaintUsaintDataResponse? = null,
    val failReason: String? = null,
)

enum class SyncStatus {
    /** rusaint 데이터 동기화 중 */
    PROCESSING,

    /** 동기화 완료 */
    COMPLETED,

    /** 토큰 만료/무효 - 재인증 필요 */
    REQUIRES_REAUTH,

    /** 학생 정보 매칭 실패 - 사용자 입력 필요 (학년, 학과, 입학년도 등) */
    REQUIRES_USER_INPUT,

    /** 동기화 실패 (서버 에러 등) */
    FAILED,
}
