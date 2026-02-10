package com.yourssu.soongpt.common.infrastructure.exception

import com.yourssu.soongpt.common.handler.InternalServerError
import org.springframework.http.HttpStatus

/**
 * rusaint-service(Python)와의 통신 중 발생하는 오류를 표현하는 예외.
 *
 * serviceStatusCode에 따라 클라이언트에 적절한 HTTP 상태코드를 반환합니다:
 * - 401: SSO 토큰 만료/무효 → 401 Unauthorized
 * - 502: 숭실대 서버 연결 실패 → 502 Bad Gateway
 * - 504: 숭실대 서버 응답 시간 초과 → 504 Gateway Timeout
 * - 500, 기타: rusaint 내부 오류 → 503 Service Unavailable
 */
class RusaintServiceException(
    message: String = "rusaint 서비스와 통신 중 오류가 발생했습니다.",
    val serviceStatusCode: Int? = null,
    val serviceDetail: String? = null,
) : InternalServerError(
    status = mapStatus(serviceStatusCode),
    message = message,
) {
    val isUnauthorized: Boolean
        get() = serviceStatusCode == 401

    companion object {
        private fun mapStatus(statusCode: Int?): HttpStatus = when (statusCode) {
            401 -> HttpStatus.UNAUTHORIZED
            502 -> HttpStatus.BAD_GATEWAY
            504 -> HttpStatus.GATEWAY_TIMEOUT
            else -> HttpStatus.SERVICE_UNAVAILABLE
        }
    }
}
