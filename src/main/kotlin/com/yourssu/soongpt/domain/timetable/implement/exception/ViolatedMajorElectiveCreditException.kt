package com.yourssu.soongpt.domain.timetable.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class ViolatedMajorElectiveCreditException: BadRequestException(message = "전공선택 희망 학점의 합이 선택된 학점의 합보다 작습니다.") {
}