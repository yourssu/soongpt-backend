package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import org.springframework.stereotype.Component

@Component
class SlackMessageProducer(
    private val slackAlarmProducer: SlackAlarmProducer,
) {
    companion object {
        private const val MESSAGE_FORMAT =
                "\uD83C\uDF89 시간표 생성 알림 \uD83C\uDF89\n" +
                "--------------------------\n" +
                "\uD83D\uDC64 학번 : %s\n" +
                "\uD83D\uDCF1 학과 : %s\n" +
                "\uD83D\uDC65 누적 시간표 생성 횟수: %s회"
    }

    fun sendTimetableCreatedMessage(request: TimetableCreatedAlarmRequest) {
        val message = MESSAGE_FORMAT.format(
            request.schoolId,
            request.departmentName,
            request.times,
        )
        slackAlarmProducer.sendAlarm(message)
    }
}