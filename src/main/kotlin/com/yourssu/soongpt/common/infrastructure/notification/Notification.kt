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

    /**
     * 학생 정보 매칭 실패 알림 (기존 방식: 로그 출력 → observer.py가 감지 후 Slack 전송)
     */
    fun notifyStudentInfoMappingFailed(
        studentIdPrefix: String,
        failureReason: String,
    ) {
        val payload = "StudentInfoMappingAlert&${mapper.writeValueAsString(mapOf(
            "studentIdPrefix" to studentIdPrefix,
            "failureReason" to failureReason,
        ))}"
        logger.warn { payload }
    }

    /**
     * Rusaint 서비스 에러/연결 실패 알림 (기존 방식: 로그 출력 → observer.py가 감지 후 Slack 전송).
     * 26학번(studentIdPrefix=2026)은 observer에서 슬랙 알림만 제외, 로깅은 그대로.
     * admissionYear: 학번 앞 4자리가 2015..2026이면 입학년도로 포함 (StudentInfoValidator와 동일 범위).
     */
    fun notifyRusaintServiceError(
        operation: String,
        statusCode: Int?,
        errorMessage: String,
        studentIdPrefix: String? = null,
    ) {
        val admissionYear = studentIdPrefix?.take(4)?.toIntOrNull()?.takeIf { it in 2015..2026 }
        val map = mutableMapOf<String, Any?>(
            "operation" to operation,
            "statusCode" to statusCode,
            "errorMessage" to errorMessage,
            "studentIdPrefix" to studentIdPrefix,
        )
        if (admissionYear != null) map["admissionYear"] = admissionYear
        val payload = "RusaintServiceError&${mapper.writeValueAsString(map)}"
        logger.warn { payload }
    }
}
