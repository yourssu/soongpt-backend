package com.yourssu.soongpt.common.infrastructure.exception

import com.yourssu.soongpt.common.handler.InternalServerError
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import org.springframework.http.HttpStatus

/**
 * 학생 정보 매칭 실패 예외.
 * 학년, 학과, 입학년도 등이 유효하지 않아 서비스를 제공할 수 없을 때 발생.
 *
 * 이 예외가 발생하면 사용자에게 직접 정보를 입력하도록 요청해야 함.
 *
 * @param partialUsaintData 검증 실패 시에도 merge된 스냅샷 데이터.
 *   수동 수정 플로우(/sync/student-info)에서 사용자가 basicInfo를 보정할 수 있도록 세션에 persist용.
 */
class StudentInfoMappingException(
    val validationError: String,
    val partialUsaintData: RusaintUsaintDataResponse? = null,
    message: String = "학생 정보 매칭에 실패했습니다. 직접 정보를 입력해주세요.",
) : InternalServerError(
    status = HttpStatus.SERVICE_UNAVAILABLE,
    message = message,
)
