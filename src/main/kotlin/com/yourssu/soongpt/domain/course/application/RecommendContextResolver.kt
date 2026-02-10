package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.handler.UnauthorizedException
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component

data class RecommendContext(
    val departmentName: String,
    val userGrade: Int,
    val schoolId: Int,
    val takenSubjectCodes: List<String>,
    val lowGradeSubjectCodes: List<String>,
    val graduationSummary: RusaintGraduationSummaryDto?,
    val flags: RusaintStudentFlagsDto,
    val warnings: List<String>,
)

@Component
class RecommendContextResolver(
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
) {
    private val logger = KotlinLogging.logger {}

    fun resolve(request: HttpServletRequest): RecommendContext {
        val pseudonym = clientJwtProvider.extractPseudonymFromRequest(request)
            .getOrElse {
                logger.warn { "JWT 추출 실패: ${it.message}" }
                throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.")
            }

        val usaintData = syncSessionStore.getUsaintData(pseudonym)
            ?: throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.")

        val takenSubjectCodes = usaintData.takenCourses
            .flatMap { it.subjectCodes }
            .distinct()

        val basicInfo = usaintData.basicInfo
        val schoolId = (basicInfo.year - basicInfo.grade + 1) % 100

        return RecommendContext(
            departmentName = basicInfo.department,
            userGrade = basicInfo.grade,
            schoolId = schoolId,
            takenSubjectCodes = takenSubjectCodes,
            lowGradeSubjectCodes = usaintData.lowGradeSubjectCodes,
            graduationSummary = usaintData.graduationSummary,
            flags = usaintData.flags,
            warnings = usaintData.warnings,
        )
    }
}
