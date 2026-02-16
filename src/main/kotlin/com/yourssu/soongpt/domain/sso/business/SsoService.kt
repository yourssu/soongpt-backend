package com.yourssu.soongpt.domain.sso.business

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import com.yourssu.soongpt.common.infrastructure.exception.StudentInfoMappingException
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.common.util.DepartmentNameNormalizer
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoResponse
import com.yourssu.soongpt.domain.sso.application.dto.StudentInfoUpdateRequest
import com.yourssu.soongpt.domain.sso.implement.SyncSession
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
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
        redirectUrl: String? = null,
    ): CallbackResult {
        logger.info { "SSO Callback - StudentId: ${studentId.take(4)}****, redirect: ${redirectUrl ?: "default"}" }

        val redirectBase = resolveRedirectBase(redirectUrl)

        // sToken 형식 검증
        if (!isValidSTokenFormat(sToken)) {
            logger.warn { "Invalid sToken format" }
            return CallbackResult(
                redirectUrl = "$redirectBase/error?reason=invalid_token",
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
                val mapped = mapFailReason(e.serviceStatusCode)
                logger.error(e) { "rusaint-service 호출 실패: ${e.message}, reason=$mapped" }
                mapped
            }
            return CallbackResult(
                redirectUrl = "$redirectBase/error?reason=$reason",
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
            redirectUrl = "$redirectBase/loading",
            authCookie = authCookie,
        )
    }

    private fun resolveRedirectBase(redirectUrl: String?): String {
        if (redirectUrl.isNullOrBlank()) {
            return ssoProperties.frontendUrl
        }

        val validatedUrl = try {
            val url = java.net.URL(redirectUrl)
            if (url.protocol !in listOf("http", "https")) {
                logger.warn { "Redirect URL with invalid protocol rejected: ${url.protocol}" }
                return ssoProperties.frontendUrl
            }
            url.toString()
        } catch (e: java.net.MalformedURLException) {
            logger.warn { "Malformed redirect URL rejected: $redirectUrl" }
            return ssoProperties.frontendUrl
        }

        if (ssoProperties.allowedRedirectUrls.contains(validatedUrl)) {
            logger.info { "Redirect URL override approved: $validatedUrl" }
            return validatedUrl
        }
        logger.warn { "Redirect URL not in allowlist, using default: $validatedUrl" }
        return ssoProperties.frontendUrl
    }

    /**
     * 동기화 상태 조회.
     */
    fun getSyncStatus(pseudonym: String): SyncSession? {
        return syncSessionStore.getSession(pseudonym)
    }

    /**
     * 사용자가 학적정보를 수정하면 캐시의 basicInfo/flags를 업데이트합니다.
     * REQUIRES_USER_INPUT(usaintData=null) 상태에서도 호출 가능: 사용자 입력만으로 최소 세션 데이터를 생성해 COMPLETED로 전환합니다.
     */
    fun updateStudentInfo(pseudonym: String, request: StudentInfoUpdateRequest): StudentInfoResponse? {
        val session = syncSessionStore.getSession(pseudonym) ?: return null

        val normalizedDepartment = DepartmentNameNormalizer.normalize(request.department)
        val normalizedDoubleMajorDepartment =
            DepartmentNameNormalizer.normalizeNullable(request.doubleMajorDepartment)
        val normalizedMinorDepartment =
            DepartmentNameNormalizer.normalizeNullable(request.minorDepartment)

        val basicInfo = RusaintBasicInfoDto(
            grade = request.grade,
            semester = request.semester,
            year = request.year,
            department = normalizedDepartment,
        )
        val flags = RusaintStudentFlagsDto(
            doubleMajorDepartment = normalizedDoubleMajorDepartment,
            minorDepartment = normalizedMinorDepartment,
            teaching = request.teaching,
        )
        val existing = session.usaintData
        val updatedData = RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = existing?.takenCourses ?: emptyList(),
            lowGradeSubjectCodes = existing?.lowGradeSubjectCodes ?: emptyList(),
            flags = flags,
            basicInfo = basicInfo,
            graduationRequirements = existing?.graduationRequirements,
            graduationSummary = existing?.graduationSummary,
            warnings = existing?.warnings ?: listOf("NO_COURSE_HISTORY", "NO_GRADUATION_DATA"),
        )

        syncSessionStore.updateStatus(pseudonym, SyncStatus.COMPLETED, updatedData)

        return StudentInfoResponse(
            grade = request.grade,
            semester = request.semester,
            year = request.year,
            department = normalizedDepartment,
            doubleMajorDepartment = normalizedDoubleMajorDepartment,
            minorDepartment = normalizedMinorDepartment,
            teaching = request.teaching,
        )
    }

    private fun mapFailReason(serviceStatusCode: Int?): String = when (serviceStatusCode) {
        502 -> "server_unreachable"
        504 -> "server_timeout"
        else -> "internal_error"
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

                val ge = usaintData.graduationSummary?.generalElective
                logger.info {
                    "[GE_DEBUG] rusaint fetch 완료: pseudonym=${pseudonym.take(8)}..., generalElective=${ge != null} (required=${ge?.required}, completed=${ge?.completed})"
                }

                // 졸업사정표에 반드시 있어야 하는 항목 누락 검증
                validateRequiredSummaryItems(usaintData)
            } catch (e: StudentInfoMappingException) {
                logger.warn { "학생 정보 매칭 실패: pseudonym=${pseudonym.take(8)}..., validation=${e.validationError}" }
                syncSessionStore.updateStatus(
                    pseudonym = pseudonym,
                    status = SyncStatus.REQUIRES_USER_INPUT,
                    usaintData = e.partialUsaintData,
                    failReason = "student_info_mapping_failed: ${e.validationError}",
                )
            } catch (e: RusaintServiceException) {
                if (e.isUnauthorized) {
                    logger.warn { "sToken 만료/무효: pseudonym=${pseudonym.take(8)}..." }
                    syncSessionStore.updateStatus(pseudonym, SyncStatus.REQUIRES_REAUTH, failReason = "token_expired")
                } else {
                    val reason = mapFailReason(e.serviceStatusCode)
                    logger.error(e) { "rusaint fetch 실패: pseudonym=${pseudonym.take(8)}..., reason=$reason" }
                    syncSessionStore.updateStatus(pseudonym, SyncStatus.FAILED, failReason = reason)
                }
            } catch (e: Exception) {
                logger.error(e) { "rusaint fetch 예외: pseudonym=${pseudonym.take(8)}..." }
                syncSessionStore.updateStatus(pseudonym, SyncStatus.FAILED, failReason = "internal_error")
            }
        }
    }

    /**
     * 졸업사정표에 반드시 있어야 하는 항목(전선/교필/교선)이 누락된 경우
     * 경고 로그 + 슬랙 알림 발송.
     * rusaint fetch 시점에 1회만 호출되어 중복 알림을 방지한다.
     */
    private fun validateRequiredSummaryItems(usaintData: RusaintUsaintDataResponse) {
        val summary = usaintData.graduationSummary ?: return // 졸업사정표 자체가 없으면 별도 처리
        val department = usaintData.basicInfo.department
        val grade = usaintData.basicInfo.grade

        val missingItems = mutableListOf<String>()
        if (summary.majorElective == null) missingItems.add("전공선택(MAJOR_ELECTIVE)")
        if (summary.generalRequired == null) missingItems.add("교양필수(GENERAL_REQUIRED)")
        if (summary.generalElective == null) missingItems.add("교양선택(GENERAL_ELECTIVE)")

        if (missingItems.isEmpty()) return

        val rawRequirements = usaintData.graduationRequirements?.requirements
        Notification.notifyGraduationSummaryParsingFailed(department, grade, missingItems, rawRequirements)
    }
}
