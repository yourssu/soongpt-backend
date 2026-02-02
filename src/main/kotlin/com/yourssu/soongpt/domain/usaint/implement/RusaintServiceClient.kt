package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.config.RusaintProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.postForEntity
import java.time.Duration

/**
 * rusaint-service (Python)와 통신하는 HTTP 클라이언트.
 *
 * - two-track: /snapshot/academic 먼저 호출 → 0.5초 후 /snapshot/graduation 호출 후 병합
 * - 통신은 내부 JWT(Authorization 헤더)로 보호한다는 가정
 */
@Component
class RusaintServiceClient(
    restTemplateBuilder: RestTemplateBuilder,
    private val rusaintProperties: RusaintProperties,
) {

    private val logger = KotlinLogging.logger {}

    private val restTemplate = restTemplateBuilder
        .rootUri(rusaintProperties.baseUrl)
        .setConnectTimeout(Duration.ofSeconds(3))
        .setReadTimeout(Duration.ofSeconds(8))
        .build()

    /**
     * rusaint-service `/api/usaint/snapshot/academic` 호출.
     * 학적/성적 이력만 조회 (졸업사정표 제외, 약 4–5초).
     */
    fun getAcademicSnapshot(
        studentId: String,
        sToken: String,
    ): RusaintAcademicResponseDto {
        val requestEntity = buildRequestEntity(studentId, sToken)
        return executeRusaintCall {
            val responseEntity = restTemplate.postForEntity<RusaintAcademicResponseDto>(
                "/api/usaint/snapshot/academic",
                requestEntity,
            )
            requireNotNull(responseEntity.body) { "Empty response from rusaint-service (academic)" }
        }
    }

    /**
     * rusaint-service `/api/usaint/snapshot/graduation` 호출.
     * 졸업사정표만 조회 (약 5–6초).
     */
    fun getGraduationSnapshot(
        studentId: String,
        sToken: String,
    ): RusaintGraduationResponseDto {
        val requestEntity = buildRequestEntity(studentId, sToken)
        return executeRusaintCall {
            val responseEntity = restTemplate.postForEntity<RusaintGraduationResponseDto>(
                "/api/usaint/snapshot/graduation",
                requestEntity,
            )
            requireNotNull(responseEntity.body) { "Empty response from rusaint-service (graduation)" }
        }
    }

    /**
     * two-track 호출: academic → 0.5초 지연 → graduation → 병합하여 [RusaintUsaintDataResponse] 반환.
     */
    fun syncUsaintData(
        studentId: String,
        sToken: String,
    ): RusaintUsaintDataResponse {
        val academic = getAcademicSnapshot(studentId, sToken)
        Thread.sleep(500)
        val graduation = getGraduationSnapshot(studentId, sToken)
        return RusaintUsaintDataResponse(
            takenCourses = academic.takenCourses,
            lowGradeSubjectCodes = academic.lowGradeSubjectCodes,
            flags = academic.flags,
            availableCredits = academic.availableCredits,
            basicInfo = academic.basicInfo,
            remainingCredits = graduation.graduationRequirements.remainingCredits,
            graduationRequirements = graduation.graduationRequirements,
        )
    }

    private fun buildRequestEntity(studentId: String, sToken: String): HttpEntity<*> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(createInternalJwt())
        }
        val body = RusaintSyncRequest(studentId = studentId, sToken = sToken)
        return HttpEntity(body, headers)
    }

    private fun createInternalJwt(): String {
        // TODO: rusaintProperties 또는 별도 설정의 시크릿/키를 이용해 실제 JWT 생성
        return "internal-jwt-placeholder"
    }

    private fun <T> executeRusaintCall(block: () -> T): T {
        return try {
            block()
        } catch (e: HttpStatusCodeException) {
            logger.error(e) { "rusaint-service 호출 실패: status=${e.statusCode.value()}" }
            throw RusaintServiceException(
                message = "rusaint 서비스 호출이 실패했습니다. (status=${e.statusCode.value()})",
            )
        } catch (e: RestClientException) {
            logger.error(e) { "rusaint-service 통신 중 예외 발생" }
            throw RusaintServiceException(
                message = "rusaint 서비스와 통신할 수 없습니다. (${e.message})",
            )
        }
    }
}

data class RusaintSyncRequest(
    val studentId: String,
    val sToken: String,
)

/** rusaint-service `/snapshot/academic` 응답 (졸업사정표 제외). */
data class RusaintAcademicResponseDto(
    val takenCourses: List<RusaintTakenCourseDto>,
    val lowGradeSubjectCodes: RusaintLowGradeSubjectCodesDto,
    val flags: RusaintStudentFlagsDto,
    val availableCredits: RusaintAvailableCreditsDto,
    val basicInfo: RusaintBasicInfoDto,
)

/** rusaint-service `/snapshot/graduation` 응답. */
data class RusaintGraduationResponseDto(
    val graduationRequirements: RusaintGraduationRequirementsDto,
)

/** academic + graduation 병합 스냅샷. */
data class RusaintUsaintDataResponse(
    val takenCourses: List<RusaintTakenCourseDto>,
    val lowGradeSubjectCodes: RusaintLowGradeSubjectCodesDto,
    val flags: RusaintStudentFlagsDto,
    val availableCredits: RusaintAvailableCreditsDto,
    val basicInfo: RusaintBasicInfoDto,
    val remainingCredits: RusaintRemainingCreditsDto,
    val graduationRequirements: RusaintGraduationRequirementsDto? = null,
)

data class RusaintTakenCourseDto(
    val year: Int,
    val semester: String,
    val subjectCodes: List<String>,
)

data class RusaintLowGradeSubjectCodesDto(
    val passLow: List<String>,
    val fail: List<String>,
)

data class RusaintStudentFlagsDto(
    val doubleMajorDepartment: String?,
    val minorDepartment: String?,
    val teaching: Boolean,
)

data class RusaintAvailableCreditsDto(
    val previousGpa: Double,
    val carriedOverCredits: Int,
    val maxAvailableCredits: Double,
)

data class RusaintBasicInfoDto(
    val year: Int,
    val semester: Int,
    val grade: Int,
    val department: String,
)

data class RusaintRemainingCreditsDto(
    val majorRequired: Int,
    val majorElective: Int,
    val generalRequired: Int,
    val generalElective: Int,
)

data class RusaintGraduationRequirementItemDto(
    val name: String,
    val requirement: Int?,
    val calculation: Double?,
    val difference: Double?,
    val result: Boolean,
    val category: String,
)

data class RusaintGraduationRequirementsDto(
    val requirements: List<RusaintGraduationRequirementItemDto>,
    val remainingCredits: RusaintRemainingCreditsDto,
)
