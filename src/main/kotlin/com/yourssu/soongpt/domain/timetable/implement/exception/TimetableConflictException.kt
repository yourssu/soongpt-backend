package com.yourssu.soongpt.domain.timetable.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class TimetableConflictException(message: String) : BadRequestException(message = message)
