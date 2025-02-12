package com.yourssu.soongpt.domain.timetable.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class TimetableNotFoundException: NotFoundException(message = "해당하는 시간표가 존재하지 않습니다.") {
}
