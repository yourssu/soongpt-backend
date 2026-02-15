package com.yourssu.soongpt.common.infrastructure.slack

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Slack Bot API 클라이언트.
 * SLACK_TOKEN + chat.postMessage API를 사용하여 알림 발송.
 * (observer.py와 동일한 인프라 사용)
 */
@Component
class SlackWebhookClient(
    @Value("\${slack.token:#{null}}") private val slackToken: String?,
    @Value("\${slack.channel:#{null}}") private val slackChannel: String?,
    @Value("\${slack.enabled:false}") private val enabled: Boolean,
    restTemplateBuilder: RestTemplateBuilder,
) {
    private val logger = KotlinLogging.logger {}
    private val restTemplate: RestTemplate = restTemplateBuilder.build()
    private val asyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val SLACK_API_URL = "https://slack.com/api/chat.postMessage"
    }

    /**
     * 학생 정보 매칭 실패 알림 (학년, 학과, 입학년도 등)
     */
    fun notifyStudentInfoMappingFailed(
        studentIdPrefix: String,
        rawData: Map<String, Any?>,
        failureReason: String,
        stackTrace: String? = null,
    ) {
        if (!isEnabled()) return

        val rawDataText = rawData.entries.joinToString("\n") { (key, value) ->
            "  • $key: ${value ?: "null"}"
        }

        val message = buildString {
            appendLine("🟡 *[학생 정보 매칭 실패]* 사용자가 직접 입력해야 함")
            appendLine()
            appendLine("*학번:* ${studentIdPrefix}****")
            appendLine("*실패 사유:* $failureReason")
            appendLine()
            appendLine("*Raw 데이터:*")
            appendLine("```")
            appendLine(rawDataText)
            appendLine("```")
            if (stackTrace != null) {
                appendLine()
                appendLine("*스택 트레이스:*")
                appendLine("```")
                appendLine(stackTrace.take(1000))
                appendLine("```")
            }
            appendLine()
            append(timestampFooter())
        }

        sendAsync(message, "학생 정보 매칭 실패 - $studentIdPrefix")
    }

    /**
     * Rusaint 서비스 에러 알림
     */
    fun notifyRusaintServiceError(
        operation: String,
        statusCode: Int?,
        errorMessage: String,
        studentIdPrefix: String? = null,
    ) {
        if (!isEnabled()) return

        val message = buildString {
            appendLine("🔴 *[Rusaint 서비스 에러]*")
            appendLine()
            appendLine("*Operation:* $operation")
            appendLine("*Status Code:* ${statusCode ?: "N/A"}")
            appendLine("*Error:* $errorMessage")
            if (studentIdPrefix != null) {
                appendLine("*학번:* ${studentIdPrefix}****")
            }
            appendLine()
            append(timestampFooter())
        }

        sendAsync(message, "Rusaint 에러 - $operation")
    }

    private fun sendAsync(message: String, logLabel: String) {
        asyncScope.launch {
            try {
                val payload = mapOf(
                    "channel" to slackChannel!!,
                    "text" to message,
                )
                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                    setBearerAuth(slackToken!!)
                }
                val entity = HttpEntity(payload, headers)

                restTemplate.postForEntity(SLACK_API_URL, entity, String::class.java)
                logger.debug { "Slack 알림 전송 완료: $logLabel" }
            } catch (e: Exception) {
                logger.error(e) { "Slack 알림 전송 실패: ${e.message}" }
            }
        }
    }

    private fun timestampFooter(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        return "*발생 시간:* $timestamp\n*서비스:* SoongPT Backend"
    }

    private fun isEnabled(): Boolean {
        return enabled && !slackToken.isNullOrBlank() && !slackChannel.isNullOrBlank()
    }
}
