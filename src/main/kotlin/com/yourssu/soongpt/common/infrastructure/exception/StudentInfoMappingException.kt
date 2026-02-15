package com.yourssu.soongpt.common.infrastructure.exception

import com.yourssu.soongpt.common.handler.InternalServerError
import org.springframework.http.HttpStatus

/**
 * 학생 정보 매칭 실패 예외.
 * 학년, 학과, 입학년도 등이 유효하지 않아 서비스를 제공할 수 없을 때 발생.
 *
 * 이 예외가 발생하면 사용자에게 직접 정보를 입력하도록 요청해야 함.
 */
class StudentInfoMappingException(
    val validationError: String,
    message: String = "학생 정보 매칭에 실패했습니다. 직접 정보를 입력해주세요.",
) : InternalServerError(
    status = HttpStatus.SERVICE_UNAVAILABLE,
    message = message,
)
