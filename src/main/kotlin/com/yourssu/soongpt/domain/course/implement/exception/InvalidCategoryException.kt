package com.yourssu.soongpt.domain.course.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class InvalidCategoryException: BadRequestException(message = "카테고리가 올바르지 않습니다.") {
}
