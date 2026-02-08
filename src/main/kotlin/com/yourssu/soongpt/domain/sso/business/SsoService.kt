package com.yourssu.soongpt.domain.sso.business

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoResponse
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoUpdateRequest
import com.yourssu.soongpt.domain.sso.implement.SyncSession
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.Cookie
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.springframework.beans.factory.DisposableBean
import org.springframework.stereotype.Service

@Service
class SsoService(
    private val ssoProperties: SsoProperties,
    private val pseudonymGenerator: PseudonymGenerator,
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
    private val rusaintServiceClient: RusaintServiceClient,
) : DisposableBean {
    private val logger = KotlinLogging.logger {}
    private val asyncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun destroy() {
        asyncScope.cancel("SsoService destroyed")
    }

    data class CallbackResult(
        val redirectUrl: String,
        val authCookie: Cookie?,
    )

    /**
     * SSO 콜백 처리.
     * 1. sToken 형식 검증
     * 2. sToken 유효성 검증 (동기 - rusaint-service 호출)
     * 3. pseudonym 생성
     * 4. JWT 쿠키 생성
     * 5. 비동기로 rusaint 데이터 fetch 시작
     * 6. 리다이렉트 URL 반환
     *
     * sToken 만료 시 즉시 에러 페이지로 리다이렉트 (폴링 대기 없이).
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

        // sToken 유효성 검증 (동기 호출 - 약 1-2초)
        try {
            rusaintServiceClient.validateToken(studentId, sToken)
        } catch (e: RusaintServiceException) {
            val reason = if (e.isUnauthorized) {
                logger.warn { "sToken 만료/무효: ${e.message}" }
                "token_expired"
            } else {
                logger.error(e) { "rusaint-service 연결 실패: ${e.message}" }
                "service_unavailable"
            }
            return CallbackResult(
                redirectUrl = "${ssoProperties.frontendUrl}/error?reason=$reason",
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

        // 비동기로 rusaint 데이터 fetch 시작
        startAsyncRusaintFetch(pseudonym, studentId, sToken)

        return CallbackResult(
            redirectUrl = "${ssoProperties.frontendUrl}/loading",
            authCookie = authCookie,
        )
    }

    /**
     * 동기화 상태 조회.
     */
    fun getSyncStatus(pseudonym: String): SyncSession? {
        return syncSessionStore.getSession(pseudonym)
    }

    /**
     * 사용자가 학적정보를 수정하면 캐시의 basicInfo/flags를 업데이트합니다.
     */
    fun updateStudentInfo(pseudonym: String, request: StudentInfoUpdateRequest): StudentInfoResponse? {
        val session = syncSessionStore.getSession(pseudonym) ?: return null
        val data = session.usaintData ?: return null

        val updatedData = data.copy(
            basicInfo = RusaintBasicInfoDto(
                grade = request.grade,
                semester = request.semester,
                year = request.year,
                department = request.department,
            ),
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = request.doubleMajorDepartment,
                minorDepartment = request.minorDepartment,
                teaching = request.teaching,
            ),
        )

        syncSessionStore.updateStatus(pseudonym, SyncStatus.COMPLETED, updatedData)

        return StudentInfoResponse(
            grade = request.grade,
            semester = request.semester,
            year = request.year,
            department = request.department,
            doubleMajorDepartment = request.doubleMajorDepartment,
            minorDepartment = request.minorDepartment,
            teaching = request.teaching,
        )
    }

    private fun isValidSTokenFormat(sToken: String): Boolean {
        // sToken은 Base64 인코딩된 문자열, 길이는 200~600자 정도
        return sToken.length in 200..700 &&
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
                if (e.isUnauthorized) {
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
