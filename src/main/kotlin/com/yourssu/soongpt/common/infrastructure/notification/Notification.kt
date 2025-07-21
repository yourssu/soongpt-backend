package com.yourssu.soongpt.common.infrastructure.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.domain.contact.business.dto.ContactResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

object Notification {
    fun notifyTimetableCreated(response: TimetableResponses) {
        logger.info { "TimetableCreated&${mapper.writeValueAsString(response)}" }
    }

    fun notifyContactCreated(response: ContactResponse) {
        logger.info { "ContactCreated&${response.id} ${response.content}" }
    }
}
