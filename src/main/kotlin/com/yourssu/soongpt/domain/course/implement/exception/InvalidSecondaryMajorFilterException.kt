package com.yourssu.soongpt.domain.course.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class InvalidSecondaryMajorFilterException(
    message: String,
) : BadRequestException(message = message)
