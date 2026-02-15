package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.handler.UnauthorizedException
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationRequirementsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

data class RecommendContext(
    val departmentName: String,
    val userGrade: Int,
    val schoolId: Int,
    /** 입학년도 (e.g. 2022, 2023). 22학번 이하 구분용 (year <= 2022) */
    val admissionYear: Int,
    val takenSubjectCodes: List<String>,
    val lowGradeSubjectCodes: List<String>,
    val graduationSummary: RusaintGraduationSummaryDto?,
    /** 졸업 요건 원본 (파싱 실패 시 슬랙 알림에 raw 데이터 첨부용) */
    val graduationRequirements: RusaintGraduationRequirementsDto?,
    val flags: RusaintStudentFlagsDto,
    val warnings: List<String>,
)

@Component
class RecommendContextResolver(
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * HttpServletRequest에서 직접 JWT를 추출하여 컨텍스트를 생성합니다.
     * 기존 코드와의 호환성을 위해 유지됩니다.
     */
    fun resolve(request: HttpServletRequest): RecommendContext {
        val pseudonym = clientJwtProvider.extractPseudonymFromRequest(request)
            .getOrElse {
                logger.warn { "JWT 추출 실패: ${it.message}" }
                throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.")
            }
        return resolveFromPseudonym(pseudonym)
    }

    /**
     * CurrentPseudonymHolder에서 pseudonym을 가져와 컨텍스트를 생성합니다.
     * CurrentPseudonymFilter가 세팅한 값을 사용합니다.
     *
     * @throws UnauthorizedException pseudonym이 없거나 세션이 만료된 경우
     */
    fun resolve(): RecommendContext {
        val pseudonym = CurrentPseudonymHolder.get()
            ?: throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.")
        return resolveFromPseudonym(pseudonym)
    }

    /**
     * CurrentPseudonymHolder에서 pseudonym을 가져와 컨텍스트를 생성합니다.
     * pseudonym이 없거나 세션이 만료된 경우 null을 반환합니다.
     *
     * @return RecommendContext 또는 null (인증되지 않은 경우)
     */
    fun resolveOptional(): RecommendContext? {
        val pseudonym = CurrentPseudonymHolder.get() ?: return null
        return try {
            resolveFromPseudonym(pseudonym)
        } catch (e: UnauthorizedException) {
            logger.debug(e) { "컨텍스트 생성 실패: ${e.message}" }
            null
        }
    }

    private fun resolveFromPseudonym(pseudonym: String): RecommendContext {
        val usaintData = syncSessionStore.getUsaintData(pseudonym)
        if (usaintData == null) {
            logger.warn {
                "캐시 미스: pseudonym=${pseudonym.take(8)}..., " +
                    "캐시 크기=${syncSessionStore.size()}, " +
                    "세션존재=${syncSessionStore.hasSession(pseudonym)}"
            }
            throw UnauthorizedException(message = "세션이 만료되었습니다. SSO 로그인을 다시 진행해 주세요.")
        }

        val takenSubjectCodes = usaintData.takenCourses
            .flatMap { it.subjectCodes }
            .distinct()

        val basicInfo = usaintData.basicInfo
        val schoolId = basicInfo.year % 100

        return RecommendContext(
            departmentName = basicInfo.department,
            userGrade = basicInfo.grade,
            schoolId = schoolId,
            admissionYear = basicInfo.year,
            takenSubjectCodes = takenSubjectCodes,
            lowGradeSubjectCodes = usaintData.lowGradeSubjectCodes,
            graduationSummary = usaintData.graduationSummary,
            graduationRequirements = usaintData.graduationRequirements,
            flags = usaintData.flags,
            warnings = usaintData.warnings,
        )
    }
}
