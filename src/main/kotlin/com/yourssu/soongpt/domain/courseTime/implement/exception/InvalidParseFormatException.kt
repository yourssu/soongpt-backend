package com.yourssu.soongpt.domain.courseTime.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class InvalidParseFormatException : BadRequestException(message = "강의 스케줄 포맷이 올바르지 않습니다.")