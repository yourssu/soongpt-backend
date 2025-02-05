package com.yourssu.soongpt.domain.course.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class InvalidTimetableRequestException : BadRequestException(message = "시간표가 나올 수 있는 경우의 수가 없습니다.") {

}
