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
 * - 통신은 항상 HTTP+TLS(인프라 레벨) + 내부 JWT(Authorization 헤더)로 보호한다는 가정
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
        .setReadTimeout(Duration.ofSeconds(5))
        .build()

    /**
     * studentId + sToken만을 이용해 rusaint-service에 u-saint 데이터 동기화를 요청합니다.
     *
     * pseudonym은 WAS 내부에서만 사용하는 식별자이므로 이 계약에 포함하지 않습니다.
     */
    fun syncUsaintData(
        studentId: String,
        sToken: String,
    ): RusaintUsaintDataResponse {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(createInternalJwt())
        }

        val body = RusaintSyncRequest(
            studentId = studentId,
            sToken = sToken,
        )

        val requestEntity = HttpEntity(body, headers)

        return executeRusaintCall {
            val responseEntity = restTemplate.postForEntity<RusaintUsaintDataResponse>(
                "/api/usaint/snapshot",
                requestEntity,
            )

            requireNotNull(responseEntity.body) {
                "Empty response from rusaint-service"
            }
        }
    }

    /**
     * WAS <-> rusaint-service 간 내부 인증용 JWT를 생성합니다.
     *
     * 현재는 구체적인 JWT 라이브러리를 도입하지 않고,
     * 이후 구현을 위한 TODO placeholder만 남겨둡니다.
     */
    private fun createInternalJwt(): String {
        // TODO: rusaintProperties 또는 별도 설정의 시크릿/키를 이용해 실제 JWT 생성
        // 예: issuer=soongpt-backend, subject=usaint-sync, short-lived 토큰 등
        return "internal-jwt-placeholder"
    }

    private fun <T> executeRusaintCall(block: () -> T): T {
        return try {
            block()
        } catch (e: HttpStatusCodeException) {
            logger.error(e) { "rusaint-service 호출 실패: status=${e.statusCode.value()}" }
            throw RusaintServiceException(
                message = "rusaint 서비스 호출이 실패했습니다. {{ status=${e.statusCode.value()} }}",
            )
        } catch (e: RestClientException) {
            logger.error(e) { "rusaint-service 통신 중 예외 발생" }
            throw RusaintServiceException(
                message = "rusaint 서비스와 통신할 수 없습니다. {{ ${e.message} }}",
            )
        }
    }
}

data class RusaintSyncRequest(
    val studentId: String,
    val sToken: String,
)

/**
 * rusaint-service에서 반환하는 u-saint 스냅샷 응답 DTO.
 */
data class RusaintUsaintDataResponse(
    val takenCourses: List<RusaintTakenCourseDto>,
    val flags: RusaintStudentFlagsDto,
    val availableCredits: RusaintAvailableCreditsDto,
    val basicInfo: RusaintBasicInfoDto,
    val remainingCredits: RusaintRemainingCreditsDto,
)

data class RusaintTakenCourseDto(
    val year: Int,
    val semester: Int,
    val subjectCode: String,
)

data class RusaintStudentFlagsDto(
    val doubleMajor: Boolean,
    val minor: Boolean,
    val teaching: Boolean,
)

data class RusaintAvailableCreditsDto(
    val previousGpa: Double,
    val carriedOverCredits: Int,
    val maxAvailableCredits: Int,
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
