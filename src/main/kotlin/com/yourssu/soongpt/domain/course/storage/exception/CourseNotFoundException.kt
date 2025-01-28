package com.yourssu.soongpt.domain.course.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class CourseNotFoundException : NotFoundException(message = "해당하는 과목이 없습니다.") {
}
