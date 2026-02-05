package com.yourssu.soongpt.domain.sso.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.domain.sso.application.dto.SyncStatusResponse
import com.yourssu.soongpt.domain.sso.business.SsoService
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.constraints.Pattern
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api")
@Validated
class SsoController(
    private val ssoService: SsoService,
    private val ssoProperties: SsoProperties,
    private val clientJwtProvider: ClientJwtProvider,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * SSO 콜백 엔드포인트.
     * 숭실대 SSO에서 로그인 완료 후 sToken, sIdno와 함께 리다이렉트됩니다.
     */
    @GetMapping("/sso/callback")
    fun ssoCallback(
        @RequestParam("sToken") sToken: String,
        @RequestParam("sIdno")
        @Pattern(regexp = "^20(1[5-9]|2[0-9])\\d{4}$", message = "Invalid student ID format")
        studentId: String,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): ResponseEntity<Void> {
        val referer = request.getHeader("Referer")

        val result = ssoService.handleCallback(
            sToken = sToken,
            studentId = studentId,
            referer = referer,
        )

        // JWT 쿠키 설정 (있는 경우만)
        result.authCookie?.let { response.addCookie(it) }

        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create(result.redirectUrl))
            .build()
    }

    /**
     * 동기화 상태 조회 엔드포인트.
     * 프론트엔드에서 폴링하여 rusaint 동기화 완료 여부를 확인합니다.
     *
     * - PROCESSING, COMPLETED: JSON 응답
     * - 그 외 (REQUIRES_REAUTH, FAILED, 세션없음): 에러 페이지로 302 리다이렉트
     */
    @GetMapping("/sync/status")
    fun getSyncStatus(
        request: HttpServletRequest,
    ): ResponseEntity<*> {
        val pseudonymResult = clientJwtProvider.extractPseudonymFromRequest(request)

        if (pseudonymResult.isFailure) {
            // 쿠키 없거나 JWT 무효 → 에러 페이지로 리다이렉트
            return redirectToError("invalid_session")
        }

        val pseudonym = pseudonymResult.getOrThrow()
        val session = ssoService.getSyncStatus(pseudonym)

        if (session == null) {
            // 세션 없음 (만료됨) → 에러 페이지로 리다이렉트
            return redirectToError("session_expired")
        }

        return when (session.status) {
            SyncStatus.PROCESSING, SyncStatus.COMPLETED -> {
                ResponseEntity.ok(
                    Response(
                        result = SyncStatusResponse(
                            status = session.status.name,
                        )
                    )
                )
            }
            SyncStatus.REQUIRES_REAUTH -> redirectToError("token_expired")
            SyncStatus.FAILED -> redirectToError("sync_failed")
        }
    }

    private fun redirectToError(reason: String): ResponseEntity<Void> {
        return ResponseEntity
            .status(HttpStatus.FOUND)
            .location(URI.create("${ssoProperties.frontendUrl}/error?reason=$reason"))
            .build()
    }
}
