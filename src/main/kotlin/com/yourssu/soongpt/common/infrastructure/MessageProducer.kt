package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest

interface MessageProducer {
    fun sendTimetableCreatedMessage(request: TimetableCreatedAlarmRequest)
}