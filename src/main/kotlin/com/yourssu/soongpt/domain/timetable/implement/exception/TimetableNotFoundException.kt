package com.yourssu.soongpt.domain.timetable.implement.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class TimetableCandidateNotGeneratedException: NotFoundException(message = "해당하는 시간표 조합이 존재하지 않습니다.") {
}
