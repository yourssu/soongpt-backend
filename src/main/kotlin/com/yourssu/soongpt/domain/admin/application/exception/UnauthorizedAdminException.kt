package com.yourssu.soongpt.domain.admin.application.exception

import com.yourssu.soongpt.common.handler.UnauthorizedException
import org.springframework.http.HttpStatus

class UnauthorizedAdminException : UnauthorizedException(
    status = HttpStatus.UNAUTHORIZED,
    message = "관리자 인증이 필요합니다."
)
