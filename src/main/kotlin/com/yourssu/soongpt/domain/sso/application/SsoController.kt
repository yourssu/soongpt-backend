package com.yourssu.soongpt.domain.sso.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoResponse
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoUpdateRequest
import com.yourssu.soongpt.domain.sso.application.dto.SyncStatusResponse
import com.yourssu.soongpt.domain.sso.business.SsoService
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import com.yourssu.soongpt.common.validation.ValidStudentId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.util.Base64

@Tag(name = "SSO", description = "SSO 인증 및 동기화 API")
@RestController
@RequestMapping("/api")
@Validated
class SsoController(
    private val ssoService: SsoService,
    private val clientJwtProvider: ClientJwtProvider,
) {
    private val logger = KotlinLogging.logger {}

    @Operation(
        summary = "SSO 콜백 (redirect 없음)",
        description = """
            숭실대 SSO 로그인 완료 후 리다이렉트를 수신합니다.
            sToken을 검증하고, JWT 쿠키를 발급한 뒤, 비동기 rusaint 데이터 동기화를 시작합니다.
            처리 완료 후 프론트엔드 동기화 페이지로 302 리다이렉트합니다.
        """,
    )
    @GetMapping("/sso/callback")
    fun ssoCallback(
        @RequestParam("sToken") sToken: String,
        @RequestParam("sIdno")
        @ValidStudentId
        studentId: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        return handleSsoCallback(sToken, studentId, redirectUrl = null, request, response)
    }

    @Operation(
        summary = "SSO 콜백 (redirect 포함)",
        description = """
            숭실대 SSO는 apiReturnUrl에 ?sToken=...을 단순 append하므로,
            redirect를 query parameter로 넘기면 ?가 두 번 생겨 파싱이 깨집니다.
            이를 우회하기 위해 redirect URL을 Base64 URL-safe로 인코딩하여 path에 포함합니다.

            예: apiReturnUrl=https://api.soongpt.com/api/sso/callback/r/{base64url(http://localhost:5173)}
        """,
    )
    @GetMapping("/sso/callback/r/{encodedRedirect}")
    fun ssoCallbackWithRedirect(
        @PathVariable encodedRedirect: String,
        @RequestParam("sToken") sToken: String,
        @RequestParam("sIdno")
        @ValidStudentId
        studentId: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val redirectUrl = try {
            String(Base64.getUrlDecoder().decode(encodedRedirect))
        } catch (e: IllegalArgumentException) {
            logger.warn(e) { "Invalid Base64 redirect (length: ${encodedRedirect.length})" }
            null
        }
        return handleSsoCallback(sToken, studentId, redirectUrl, request, response)
    }

    private fun handleSsoCallback(
        sToken: String,
        studentId: String,
        redirectUrl: String?,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val referer = request.getHeader("Referer")

        val result = ssoService.handleCallback(
            sToken = sToken,
            studentId = studentId,
            referer = referer,
            redirectUrl = redirectUrl,
        )

        // JWT 쿠키 설정 (있는 경우만)
        result.authCookie?.let { response.addCookie(it) }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(result.redirectUrl))
            .build()
    }

    @Operation(
        summary = "동기화 상태 조회",
        description = """
            쿠키 기반 인증으로 rusaint 동기화 진행 상태를 조회합니다.
            프론트엔드에서 fetch 폴링하여 사용하며, 모든 응답은 JSON입니다 (302 리다이렉트 없음).
            프론트는 HTTP 상태코드 + result.status + reason 값으로 분기합니다.

            **status별 응답:**
            - 200 PROCESSING: 동기화 진행 중 (계속 폴링)
            - 200 COMPLETED: 동기화 완료 (studentInfo 포함)
            - 200 REQUIRES_REAUTH: sToken 만료, 재인증 필요 (reason: token_expired)
            - 200 REQUIRES_USER_INPUT: 학생 정보 매칭 실패, 사용자 입력 필요 (reason: student_info_mapping_failed)
            - 200 FAILED: 동기화 실패 (reason: server_unreachable | server_timeout | internal_error)
            - 401 ERROR: 쿠키/JWT 문제 (reason: invalid_session | session_expired)

            **reason 값 설명:**
            - invalid_session: 쿠키가 없거나 JWT가 유효하지 않음 → 재로그인
            - session_expired: 동기화 세션 TTL 만료 → 재로그인
            - token_expired: 유세인트 sToken 만료 → SSO 재인증
            - student_info_mapping_failed: 학년/학과/입학년도 매칭 실패 → 사용자 직접 입력
            - server_unreachable: 유세인트 서버 접속 불가 → 잠시 후 재시도
            - server_timeout: 유세인트 서버 응답 시간 초과 → 잠시 후 재시도
            - internal_error: 내부 서버 오류 → 관리자 문의
        """,
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "동기화 상태 (result.status: PROCESSING | COMPLETED | REQUIRES_REAUTH | REQUIRES_USER_INPUT | FAILED)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = SyncStatusResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "쿠키/JWT 없음 또는 무효 (result.status=ERROR, reason: invalid_session | session_expired)",
                content = [Content(mediaType = "application/json", schema = Schema(implementation = SyncStatusResponse::class))],
            ),
        ],
    )
    @GetMapping("/sync/status")
    fun getSyncStatus(
        @CookieValue(name = "soongpt_auth", required = false) soongptAuth: String?,
    ): ResponseEntity<Response<SyncStatusResponse>> {
        if (soongptAuth == null) {
            return jsonResponse(HttpStatus.UNAUTHORIZED, "ERROR", reason = "invalid_session")
        }

        val pseudonymResult = clientJwtProvider.validateAndGetPseudonym(soongptAuth)

        if (pseudonymResult.isFailure) {
            return jsonResponse(HttpStatus.UNAUTHORIZED, "ERROR", reason = "invalid_session")
        }

        val pseudonym = pseudonymResult.getOrThrow()
        val session = ssoService.getSyncStatus(pseudonym)

        if (session == null) {
            return jsonResponse(HttpStatus.UNAUTHORIZED, "ERROR", reason = "session_expired")
        }

        return when (session.status) {
            SyncStatus.PROCESSING -> {
                jsonResponse(HttpStatus.OK, "PROCESSING")
            }
            SyncStatus.COMPLETED -> {
                val data = session.usaintData
                val studentInfo = data?.let {
                    StudentInfoResponse(
                        grade = it.basicInfo.grade,
                        semester = it.basicInfo.semester,
                        year = it.basicInfo.year,
                        department = it.basicInfo.department,
                        doubleMajorDepartment = it.flags.doubleMajorDepartment,
                        minorDepartment = it.flags.minorDepartment,
                        teaching = it.flags.teaching,
                    )
                }
                val warnings = data?.warnings?.ifEmpty { null }
                jsonResponse(HttpStatus.OK, "COMPLETED", studentInfo = studentInfo, warnings = warnings)
            }
            SyncStatus.REQUIRES_REAUTH -> jsonResponse(HttpStatus.OK, "REQUIRES_REAUTH", reason = session.failReason ?: "token_expired")
            SyncStatus.REQUIRES_USER_INPUT -> jsonResponse(HttpStatus.OK, "REQUIRES_USER_INPUT", reason = session.failReason ?: "student_info_mapping_failed")
            SyncStatus.FAILED -> jsonResponse(HttpStatus.OK, "FAILED", reason = session.failReason ?: "internal_error")
        }
    }

    @Operation(
        summary = "학적정보 수정",
        description = """
            동기화된 학적정보가 틀린 경우 사용자가 직접 수정합니다.
            수정된 정보는 캐시에 반영되어 이후 화면별 API에서 사용됩니다.
        """,
    )
    @PutMapping("/sync/student-info")
    fun updateStudentInfo(
        @CookieValue(name = "soongpt_auth", required = false) soongptAuth: String?,
        @RequestBody request: StudentInfoUpdateRequest,
    ): ResponseEntity<Response<SyncStatusResponse>> {
        if (soongptAuth == null) {
            return jsonResponse(HttpStatus.UNAUTHORIZED, "ERROR", reason = "invalid_session")
        }

        val pseudonymResult = clientJwtProvider.validateAndGetPseudonym(soongptAuth)

        if (pseudonymResult.isFailure) {
            return jsonResponse(HttpStatus.UNAUTHORIZED, "ERROR", reason = "invalid_session")
        }

        val pseudonym = pseudonymResult.getOrThrow()
        val updated = ssoService.updateStudentInfo(pseudonym, request)
            ?: return jsonResponse(HttpStatus.UNAUTHORIZED, "ERROR", reason = "session_expired")

        return jsonResponse(HttpStatus.OK, "COMPLETED", studentInfo = updated)
    }

    private fun jsonResponse(
        httpStatus: HttpStatus,
        status: String,
        reason: String? = null,
        studentInfo: StudentInfoResponse? = null,
        warnings: List<String>? = null,
    ): ResponseEntity<Response<SyncStatusResponse>> {
        return ResponseEntity
            .status(httpStatus)
            .body(
                Response(
                    result = SyncStatusResponse(
                        status = status,
                        reason = reason,
                        studentInfo = studentInfo,
                        warnings = warnings,
                    )
                )
            )
    }
}
