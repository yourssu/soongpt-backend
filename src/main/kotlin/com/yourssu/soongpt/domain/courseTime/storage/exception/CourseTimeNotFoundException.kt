package com.yourssu.soongpt.domain.courseTime.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class CourseTimeNotFoundException: NotFoundException(message = "해당하는 과목 시간이 없습니다.") {
}
