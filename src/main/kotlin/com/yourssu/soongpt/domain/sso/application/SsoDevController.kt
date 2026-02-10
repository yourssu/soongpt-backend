package com.yourssu.soongpt.domain.sso.application

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintChapelSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "SSO Dev", description = "로컬 테스트용 (local 프로필에서만 활성화)")
@Profile("local")
@RestController
@RequestMapping("/api/dev")
class SsoDevController(
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
) {
    @Operation(
        summary = "테스트용 토큰 발급 + 쿠키 설정",
        description = """
            로컬 테스트용으로 mock 세션 + JWT를 발급하고, 브라우저에 쿠키를 직접 심습니다.
            이 요청 실행 후 Swagger에서 /api/sync/status 등을 바로 테스트할 수 있습니다.
        """,
    )
    @PostMapping("/test-token")
    fun issueTestToken(response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val pseudonym = "test-pseudonym-local"

        syncSessionStore.createSession(pseudonym)
        syncSessionStore.updateStatus(
            pseudonym = pseudonym,
            status = SyncStatus.COMPLETED,
            usaintData = RusaintUsaintDataResponse(
                pseudonym = pseudonym,
                takenCourses = emptyList(),
                lowGradeSubjectCodes = emptyList(),
                flags = RusaintStudentFlagsDto(
                    doubleMajorDepartment = null,
                    minorDepartment = null,
                    teaching = false,
                ),
                basicInfo = RusaintBasicInfoDto(
                    year = 2023,
                    semester = 5,
                    grade = 3,
                    department = "소프트웨어학부",
                ),
                graduationSummary = RusaintGraduationSummaryDto(
                    generalRequired = RusaintCreditSummaryItemDto(
                        required = 19,
                        completed = 17,
                        satisfied = false,
                    ),
                    generalElective = RusaintCreditSummaryItemDto(
                        required = 12,
                        completed = 15,
                        satisfied = true,
                    ),
                    majorFoundation = RusaintCreditSummaryItemDto(
                        required = 15,
                        completed = 9,
                        satisfied = false,
                    ),
                    majorRequired = RusaintCreditSummaryItemDto(
                        required = 21,
                        completed = 12,
                        satisfied = false,
                    ),
                    majorElective = RusaintCreditSummaryItemDto(
                        required = 30,
                        completed = 18,
                        satisfied = false,
                    ),
                    doubleMajorRequired = RusaintCreditSummaryItemDto(
                        required = 0,
                        completed = 0,
                        satisfied = true,
                    ),
                    doubleMajorElective = RusaintCreditSummaryItemDto(
                        required = 0,
                        completed = 0,
                        satisfied = true,
                    ),
                    minor = RusaintCreditSummaryItemDto(
                        required = 0,
                        completed = 0,
                        satisfied = true,
                    ),
                    christianCourses = RusaintCreditSummaryItemDto(
                        required = 6,
                        completed = 6,
                        satisfied = true,
                    ),
                    chapel = RusaintChapelSummaryItemDto(
                        satisfied = true,
                    ),
                ),
            ),
        )

        val token = clientJwtProvider.issueToken(pseudonym)

        // Swagger에서 바로 사용 가능하도록 쿠키를 직접 설정 (local에서는 Secure 제거)
        response.addHeader(
            "Set-Cookie",
            "soongpt_auth=$token; Path=/; HttpOnly; SameSite=Lax; Max-Age=3600",
        )

        return ResponseEntity.ok(
            mapOf(
                "message" to "쿠키가 설정되었습니다. 이제 Swagger에서 다른 API를 테스트하세요.",
                "soongpt_auth" to token,
            )
        )
    }
}
