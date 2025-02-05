package com.yourssu.soongpt.domain.course.implement.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class CourseNotFoundException(
    val courseName: String = "",
) : NotFoundException(message = "해당하는 과목이 없습니다. 과목 이름: {$courseName}") {
}
