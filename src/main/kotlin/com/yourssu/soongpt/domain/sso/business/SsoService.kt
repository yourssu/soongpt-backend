package com.yourssu.soongpt.domain.sso.business

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import com.yourssu.soongpt.domain.sso.implement.SyncSession
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Service

@Service
class SsoService(
    private val ssoProperties: SsoProperties,
    private val pseudonymGenerator: PseudonymGenerator,
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
    private val rusaintServiceClient: RusaintServiceClient,
) {
    private val logger = KotlinLogging.logger {}
    private val asyncScope = CoroutineScope(Dispatchers.IO)

    data class CallbackResult(
        val redirectUrl: String,
        val authCookie: Cookie?,
    )

    /**
     * SSO 콜백 처리.
     * 1. sToken 형식/재사용 검증
     * 2. pseudonym 생성
     * 3. JWT 쿠키 생성
     * 4. 비동기로 rusaint fetch 시작
     * 5. 리다이렉트 URL 반환
     */
    fun handleCallback(
        sToken: String,
        studentId: String,
        referer: String?,
    ): CallbackResult {
        // Referer 로깅 (DEBUG - 배포 시 출력 안 됨)
        logger.debug { "SSO Callback - Referer: $referer, StudentId: ${studentId.take(4)}****" }

        // sToken 형식 검증
        if (!isValidSTokenFormat(sToken)) {
            logger.warn { "Invalid sToken format" }
            return CallbackResult(
                redirectUrl = "${ssoProperties.frontendUrl}/error?reason=invalid_token",
                authCookie = null,
            )
        }

        // pseudonym 생성
        val pseudonym = pseudonymGenerator.generate(studentId)

        // 세션 생성 (PROCESSING 상태)
        syncSessionStore.createSession(pseudonym)

        // JWT 쿠키 생성
        val token = clientJwtProvider.issueToken(pseudonym)
        val authCookie = clientJwtProvider.createAuthCookie(token)

        // 비동기로 rusaint fetch 시작
        startAsyncRusaintFetch(pseudonym, studentId, sToken)

        return CallbackResult(
            redirectUrl = ssoProperties.frontendUrl,
            authCookie = authCookie,
        )
    }

    /**
     * 동기화 상태 조회.
     */
    fun getSyncStatus(pseudonym: String): SyncSession? {
        return syncSessionStore.getSession(pseudonym)
    }

    private fun isValidSTokenFormat(sToken: String): Boolean {
        // sToken은 Base64 인코딩된 문자열, 길이는 200~600자 정도
        return sToken.length in 100..1000 &&
            sToken.matches(Regex("^[A-Za-z0-9+/=_-]+$"))
    }

    private fun startAsyncRusaintFetch(pseudonym: String, studentId: String, sToken: String) {
        asyncScope.launch {
            try {
                logger.info { "rusaint fetch 시작: pseudonym=${pseudonym.take(8)}..." }

                val usaintData = rusaintServiceClient.syncUsaintData(
                    studentId = studentId,
                    sToken = sToken,
                )

                syncSessionStore.updateStatus(
                    pseudonym = pseudonym,
                    status = SyncStatus.COMPLETED,
                    usaintData = usaintData,
                )

                logger.info { "rusaint fetch 완료: pseudonym=${pseudonym.take(8)}..." }
            } catch (e: RusaintServiceException) {
                // 401 에러 (토큰 만료/무효)인 경우
                if (e.message?.contains("401") == true ||
                    e.message?.contains("invalid or expired") == true
                ) {
                    logger.warn { "sToken 만료/무효: pseudonym=${pseudonym.take(8)}..." }
                    syncSessionStore.updateStatus(pseudonym, SyncStatus.REQUIRES_REAUTH)
                } else {
                    logger.error(e) { "rusaint fetch 실패: pseudonym=${pseudonym.take(8)}..." }
                    syncSessionStore.updateStatus(pseudonym, SyncStatus.FAILED)
                }
            } catch (e: Exception) {
                logger.error(e) { "rusaint fetch 예외: pseudonym=${pseudonym.take(8)}..." }
                syncSessionStore.updateStatus(pseudonym, SyncStatus.FAILED)
            }
        }
    }
}
