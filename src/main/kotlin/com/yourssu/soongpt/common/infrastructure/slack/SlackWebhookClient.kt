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
 * Slack 웹훅 클라이언트.
 * 학년/학과/입학년도 매칭 실패 등 중요한 데이터 이슈를 Slack으로 알림.
 */
@Component
class SlackWebhookClient(
    @Value("\${slack.webhook-url:#{null}}") private val webhookUrl: String?,
    @Value("\${slack.enabled:false}") private val enabled: Boolean,
    restTemplateBuilder: RestTemplateBuilder,
) {
    private val logger = KotlinLogging.logger {}
    private val restTemplate: RestTemplate = restTemplateBuilder.build()
    private val asyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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

        asyncScope.launch {
            try {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

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
                        appendLine(stackTrace.take(1000)) // 최대 1000자
                        appendLine("```")
                    }
                    appendLine()
                    appendLine("*발생 시간:* $timestamp")
                    appendLine("*서비스:* SoongPT Backend")
                }

                val payload = mapOf("text" to message.toString())
                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }
                val entity = HttpEntity(payload, headers)

                restTemplate.postForEntity(webhookUrl!!, entity, String::class.java)
                logger.debug { "Slack 알림 전송 완료: 학생 정보 매칭 실패 - $studentIdPrefix" }
            } catch (e: Exception) {
                logger.error(e) { "Slack 알림 전송 실패: ${e.message}" }
            }
        }
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

        asyncScope.launch {
            try {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

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
                    appendLine("*발생 시간:* $timestamp")
                    appendLine("*서비스:* SoongPT Backend")
                }

                val payload = mapOf("text" to message.toString())
                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }
                val entity = HttpEntity(payload, headers)

                restTemplate.postForEntity(webhookUrl!!, entity, String::class.java)
                logger.debug { "Slack 알림 전송 완료: Rusaint 에러 - $operation" }
            } catch (e: Exception) {
                logger.error(e) { "Slack 알림 전송 실패: ${e.message}" }
            }
        }
    }

    /**
     * 졸업사정표 파싱 실패 알림 (전선/교필/교선 등 반드시 있어야 하는 항목이 null)
     */
    fun notifyGraduationSummaryParsingFailed(
        departmentName: String,
        userGrade: Int,
        category: String,
    ) {
        if (!isEnabled()) return

        asyncScope.launch {
            try {
                val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))

                val message = buildString {
                    appendLine("🟠 *[졸업사정표 파싱 실패]* $category 항목 누락")
                    appendLine()
                    appendLine("*학과:* $departmentName")
                    appendLine("*학년:* ${userGrade}학년")
                    appendLine("*누락 항목:* $category")
                    appendLine("*영향:* 이수현황 미표시 (progress -2), 과목 추천은 정상 제공")
                    appendLine()
                    appendLine("*조치 필요:* `graduation_summary_builder.py` 파서가 해당 학과의 항목명을 인식하지 못할 수 있습니다.")
                    appendLine("*발생 시간:* $timestamp")
                    appendLine("*서비스:* SoongPT Backend")
                }

                val payload = mapOf("text" to message.toString())
                val headers = HttpHeaders().apply {
                    contentType = MediaType.APPLICATION_JSON
                }
                val entity = HttpEntity(payload, headers)

                restTemplate.postForEntity(webhookUrl!!, entity, String::class.java)
                logger.debug { "Slack 알림 전송 완료: 졸업사정표 파싱 실패 - $departmentName $category" }
            } catch (e: Exception) {
                logger.error(e) { "Slack 알림 전송 실패: ${e.message}" }
            }
        }
    }

    private fun isEnabled(): Boolean {
        return enabled && !webhookUrl.isNullOrBlank()
    }
}
