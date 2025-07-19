package com.yourssu.soongpt.common.infrastructure.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

object Notification {
    fun notifyTimetableCreated(request: TimetableResponses) {
        logger.info { "TimetableCreated&${mapper.writeValueAsString(request)}" }
    }
}
