package com.yourssu.soongpt.common.infrastructure.exception

import com.yourssu.soongpt.common.handler.InternalServerError
import org.springframework.http.HttpStatus

/**
 * rusaint-service(Python)와의 통신 중 발생하는 오류를 표현하는 예외.
 *
 * - 주로 네트워크 오류, 타임아웃, 5xx 등 외부 서비스 장애를 표현합니다.
 * - 클라이언트에는 503(Service Unavailable) 상태코드로 매핑됩니다.
 */
class RusaintServiceException(
    message: String = "rusaint 서비스와 통신 중 오류가 발생했습니다.",
) : InternalServerError(
    status = HttpStatus.SERVICE_UNAVAILABLE,
    message = message,
)
