package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.config.RusaintProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.postForEntity
import java.time.Duration

/**
 * rusaint-service (Python)와 통신하는 HTTP 클라이언트.
 *
 * 책임: HTTP 호출 및 예외 변환만 담당.
 * - two-track: /snapshot/academic 먼저 호출 → 0.5초 후 /snapshot/graduation 호출 후 병합
 * - 요청 생성은 [RusaintRequestBuilder], 병합은 [RusaintSnapshotMerger]에 위임.
 */
@Component
class RusaintServiceClient(
    restTemplateBuilder: RestTemplateBuilder,
    rusaintProperties: RusaintProperties,
    private val rusaintRequestBuilder: RusaintRequestBuilder,
    private val rusaintSnapshotMerger: RusaintSnapshotMerger,
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
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
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
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
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
        return rusaintSnapshotMerger.merge(academic, graduation)
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
