package com.yourssu.soongpt.common.infrastructure.slack

import com.yourssu.soongpt.common.business.dto.Response
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.*

/**
 * Slack 알림 테스트용 컨트롤러 (local 환경에서만 활성화)
 */
@Tag(name = "Admin", description = "관리자 및 테스트 API")
@RestController
@RequestMapping("/api/test/slack")
@Profile("local")
class SlackTestController(
    private val slackWebhookClient: SlackWebhookClient,
) {

    @Operation(
        summary = "학생 정보 매칭 실패 알림 테스트",
        description = "실제 Slack으로 학생 정보 매칭 실패 알림을 전송합니다. (local 환경에서만 사용 가능)"
    )
    @PostMapping("/student-info-mapping-failed")
    fun testStudentInfoMappingFailed(
        @RequestBody request: TestStudentInfoMappingRequest
    ): Response<TestNotificationResponse> {
        slackWebhookClient.notifyStudentInfoMappingFailed(
            studentIdPrefix = request.studentIdPrefix ?: "TEST",
            rawData = request.rawData ?: mapOf(
                "grade" to 6,
                "semester" to 3,
                "year" to 2014,
                "department" to "컴공학부",
                "taken_courses_count" to 45
            ),
            failureReason = request.failureReason ?: "테스트: 학년이 유효하지 않음: 6 (예상 범위: 1~5)"
        )

        return Response(
            result = TestNotificationResponse(
                message = "Slack 알림이 전송되었습니다. 채널을 확인하세요.",
                webhookUrl = "확인됨",
                enabled = true
            )
        )
    }

    @Operation(
        summary = "Rusaint 서비스 에러 알림 테스트",
        description = "실제 Slack으로 Rusaint 서비스 에러 알림을 전송합니다. (local 환경에서만 사용 가능)"
    )
    @PostMapping("/rusaint-service-error")
    fun testRusaintServiceError(
        @RequestBody request: TestRusaintErrorRequest
    ): Response<TestNotificationResponse> {
        slackWebhookClient.notifyRusaintServiceError(
            operation = request.operation ?: "test-operation",
            statusCode = request.statusCode ?: 502,
            errorMessage = request.errorMessage ?: "테스트: 숭실대 서버 연결 실패",
            studentIdPrefix = request.studentIdPrefix
        )

        return Response(
            result = TestNotificationResponse(
                message = "Slack 알림이 전송되었습니다. 채널을 확인하세요.",
                webhookUrl = "확인됨",
                enabled = true
            )
        )
    }

    @Operation(
        summary = "Slack 설정 확인",
        description = "현재 Slack 알림이 활성화되어 있는지 확인합니다."
    )
    @GetMapping("/config")
    fun getSlackConfig(): Response<SlackConfigResponse> {
        // SlackWebhookClient의 enabled와 webhookUrl을 reflection으로 가져올 수 있지만
        // 여기서는 간단하게 테스트 메시지 전송 여부만 확인
        return Response(
            result = SlackConfigResponse(
                message = "Slack 설정을 확인하려면 알림 전송 테스트를 해보세요.",
                hint = "POST /api/test/slack/student-info-mapping-failed 호출"
            )
        )
    }
}

data class TestStudentInfoMappingRequest(
    val studentIdPrefix: String? = null,
    val rawData: Map<String, Any?>? = null,
    val failureReason: String? = null,
)

data class TestRusaintErrorRequest(
    val operation: String? = null,
    val statusCode: Int? = null,
    val errorMessage: String? = null,
    val studentIdPrefix: String? = null,
)

data class TestNotificationResponse(
    val message: String,
    val webhookUrl: String,
    val enabled: Boolean,
)

data class SlackConfigResponse(
    val message: String,
    val hint: String,
)
