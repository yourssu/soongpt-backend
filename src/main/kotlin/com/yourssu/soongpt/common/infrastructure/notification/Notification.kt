package com.yourssu.soongpt.common.infrastructure.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.domain.contact.business.dto.ContactResponse
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationRequirementItemDto
import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.Base64

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

object Notification {
    fun notifyTimetableCreated(request: TimetableCreatedAlarmRequest) {
        logger.info { "TimetableCreated&${mapper.writeValueAsString(request)}" }
    }

    fun notifyContactCreated(response: ContactResponse) {
        logger.info { "ContactCreated&${response.id}" }
    }

    fun notifyGraduationSummaryParsingFailed(
        departmentName: String,
        userGrade: Int,
        missingItems: List<String>,
        rawRequirements: List<RusaintGraduationRequirementItemDto>? = null,
    ) {
        val rawBase64 = rawRequirements
            ?.takeIf { it.isNotEmpty() }
            ?.let { Base64.getEncoder().encodeToString(mapper.writeValueAsBytes(it)) }
            ?: ""

        val payload = buildString {
            append("GraduationSummaryAlert&{departmentName:$departmentName,userGrade:$userGrade,missingItems:${missingItems.joinToString(";")}")
            if (rawBase64.isNotEmpty()) append(",rawDataBase64:$rawBase64")
            append("}")
        }
        logger.warn { payload }
    }
}
