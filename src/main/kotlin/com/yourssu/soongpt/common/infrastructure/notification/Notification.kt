package com.yourssu.soongpt.common.infrastructure.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.domain.contact.business.dto.ContactResponse
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

object Notification {
    fun notifyTimetableCreated(request: TimetableCreatedAlarmRequest) {
        logger.info { "TimetableCreated&${mapper.writeValueAsString(request)}" }
    }

    fun notifyContactCreated(response: ContactResponse) {
        logger.info { "ContactCreated&${response.id}" }
    }
}
