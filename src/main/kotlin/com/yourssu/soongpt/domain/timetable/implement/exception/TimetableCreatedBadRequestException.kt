package com.yourssu.soongpt.domain.timetable.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class TimetableCreatedBadRequestException : BadRequestException(message = "선택한 과목 조합으로 생성한 시간표가 없습니다. 선택한 과목을 다시 확인해주세요.") {
}
