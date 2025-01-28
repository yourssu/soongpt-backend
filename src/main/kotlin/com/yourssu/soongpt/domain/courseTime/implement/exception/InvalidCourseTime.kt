package com.yourssu.soongpt.domain.courseTime.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class InvalidCourseTime : BadRequestException(message = "강의 시작 시간이 강의 종료 시간보다 늦습니다.") {
}