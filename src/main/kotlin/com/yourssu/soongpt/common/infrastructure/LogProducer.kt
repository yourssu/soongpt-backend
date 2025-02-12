package com.yourssu.soongpt.common.infrastructure

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

@Component
@Profile("!prod")
class LogProducer : MessageProducer {
    override fun sendTimetableCreatedMessage(request: TimetableCreatedAlarmRequest) {
        logger.info { "Sent Alarm: ${mapper.writeValueAsString(request)}" }
    }
}