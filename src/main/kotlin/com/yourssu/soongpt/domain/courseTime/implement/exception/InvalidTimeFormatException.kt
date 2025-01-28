package com.yourssu.soongpt.domain.courseTime.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class InvalidTimeFormatException : BadRequestException(message = "시간 형식이 올바르지 않습니다.")