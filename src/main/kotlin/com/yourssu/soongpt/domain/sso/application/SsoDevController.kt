package com.yourssu.soongpt.domain.sso.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.domain.course.business.UntakenCourseCodeService
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.sso.implement.MockUsaintData
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "SSO Dev", description = "로컬/개발 테스트용 (local, dev 프로필에서 활성화)")
@Profile("local")
@RestController
@RequestMapping("/api/dev")
class SsoDevController(
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
    private val untakenCourseCodeService: UntakenCourseCodeService,
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

        // 운영 쿠키 설정과 동일한 속성 적용
        response.addCookie(clientJwtProvider.createAuthCookie(token))

        return ResponseEntity.ok(
            mapOf(
                "message" to "쿠키가 설정되었습니다. 이제 Swagger에서 다른 API를 테스트하세요.",
                "soongpt_auth" to token,
            )
        )
    }

    @Operation(
        summary = "Mock 사용자 토큰 발급 (테스트용)",
        description = """
            MockUsaintData.build() 로 만든 mock usaint 데이터로 세션을 만들고, 해당 사용자용 JWT를 발급해 쿠키에 넣습니다.
            MockUsaintData.kt 에서 필드(학과·학년·이수과목·졸업사정표 등)를 채운 뒤 이 API를 호출하면,
            추천 API(GET /api/courses/recommend/all 등)를 같은 쿠키로 테스트할 수 있습니다.
        """,
    )
    @PostMapping("/mock-user-token")
    fun issueMockUserToken(response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val pseudonym = MockUsaintData.MOCK_USER_PSEUDONYM
        val mockData = MockUsaintData.build()

        syncSessionStore.createSession(pseudonym)
        syncSessionStore.updateStatus(
            pseudonym = pseudonym,
            status = SyncStatus.COMPLETED,
            usaintData = mockData,
        )

        val token = clientJwtProvider.issueToken(pseudonym)
        response.addCookie(clientJwtProvider.createAuthCookie(token))

        return ResponseEntity.ok(
            mapOf(
                "message" to "Mock 사용자 세션·쿠키가 설정되었습니다. MockUsaintData.kt 값을 수정한 뒤 재호출하면 반영됩니다.",
                "pseudonym" to pseudonym,
            )
        )
    }

    @Operation(
        summary = "복수전공·부전공 테스트용 Mock 토큰 발급",
        description = """
            MockUsaintData.buildForDoubleMajorAndMinor() 로 만든 mock 데이터로 세션·쿠키를 설정합니다.
            복필/복선/부전공 추천 API 테스트 시: MockUsaintData.kt 의 buildForDoubleMajorAndMinor() 에서
            doubleMajorDepartment, minorDepartment, basicInfo.department, graduationSummary(복필/복선/부전공 학점) 등을
            채운 뒤 이 API를 호출하고, 같은 쿠키로 GET /api/courses/recommend/all?category=DOUBLE_MAJOR_REQUIRED,DOUBLE_MAJOR_ELECTIVE,MINOR 호출하면 됩니다.
        """,
    )
    @PostMapping("/mock-double-major-token")
    fun issueMockDoubleMajorToken(response: HttpServletResponse): ResponseEntity<Map<String, String>> {
        val pseudonym = MockUsaintData.MOCK_USER_PSEUDONYM
        val mockData = MockUsaintData.buildForDoubleMajorAndMinor()

        syncSessionStore.createSession(pseudonym)
        syncSessionStore.updateStatus(
            pseudonym = pseudonym,
            status = SyncStatus.COMPLETED,
            usaintData = mockData,
        )

        val token = clientJwtProvider.issueToken(pseudonym)
        response.addCookie(clientJwtProvider.createAuthCookie(token))

        return ResponseEntity.ok(
            mapOf(
                "message" to "복수전공·부전공 테스트용 세션·쿠키가 설정되었습니다. MockUsaintData.buildForDoubleMajorAndMinor() 값을 채운 뒤 재호출하면 반영됩니다.",
                "pseudonym" to pseudonym,
            )
        )
    }

    @Operation(
        summary = "미수강 과목코드 조회 (UntakenCourseCodeService 테스트용)",
        description = """
            mock-user-token 으로 세션을 만든 뒤, category를 지정해서 호출하면
            UntakenCourseCodeService의 raw 결과를 바로 볼 수 있습니다.
            - 전기/전필/전선: List<Long> (10자리 과목코드)
            - 교필/교선: Map<분야명, List<Long>>
        """,
    )
    @GetMapping("/untaken-codes")
    fun getUntakenCodes(
        @RequestParam category: Category,
    ): ResponseEntity<Response<Any>> {
        val result: Any = when (category) {
            Category.GENERAL_REQUIRED, Category.GENERAL_ELECTIVE ->
                untakenCourseCodeService.getUntakenCourseCodesByField(category)
            else ->
                untakenCourseCodeService.getUntakenCourseCodes(category)
        }
        return ResponseEntity.ok(Response(result = result))
    }
}
