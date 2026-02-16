package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.config.RusaintProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.common.infrastructure.exception.StudentInfoMappingException
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.postForEntity
import java.time.Duration
import java.util.function.Supplier

/** rusaint-service(Python) HTTP 클라이언트. academic·graduation 병렬 호출 후 병합. */
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
        .requestFactory(
            Supplier {
                val simple = SimpleClientHttpRequestFactory().apply {
                    setConnectTimeout(Duration.ofSeconds(15))
                    setReadTimeout(Duration.ofSeconds(60))
                }
                BufferingClientHttpRequestFactory(simple)
            },
        )
        .build()

    fun getAcademicSnapshot(
        studentId: String,
        sToken: String,
    ): RusaintAcademicResponseDto? {
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
        return try {
            executeRusaintCall("academic", studentId.take(4)) {
                val responseEntity = restTemplate.postForEntity<RusaintAcademicResponseDto>(
                    "/api/usaint/snapshot/academic",
                    requestEntity,
                )
                requireNotNull(responseEntity.body) { "Empty response from rusaint-service (academic)" }
            }
        } catch (e: RusaintServiceException) {
            if (e.isUnauthorized || e.serviceStatusCode in listOf(502, 504)) {
                throw e
            }
            // 500 등 기본 학적 정보 파싱 실패(데이터 없음) → null 반환 → REQUIRES_USER_INPUT 처리
            logger.warn { "학적/성적 조회 실패 (기본 정보 없음 가능성), null 반환: status=${e.serviceStatusCode}, detail=${e.serviceDetail}" }
            null
        }
    }

    fun getGraduationSnapshot(
        studentId: String,
        sToken: String,
    ): RusaintGraduationResponseDto? {
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
        return try {
            executeRusaintCall("graduation", studentId.take(4)) {
                val responseEntity = restTemplate.postForEntity<RusaintGraduationResponseDto>(
                    "/api/usaint/snapshot/graduation",
                    requestEntity,
                )
                requireNotNull(responseEntity.body) { "Empty response from rusaint-service (graduation)" }
            }
        } catch (e: RusaintServiceException) {
            // 401/502/504 인프라 에러는 그대로 throw
            if (e.isUnauthorized || e.serviceStatusCode in listOf(502, 504)) {
                throw e
            }
            // 500 등 데이터 없음 에러 → null 반환 (새내기 등)
            // TODO(PT-134): rusaint 라이브러리가 "데이터 없음"과 "파싱 오류"를 구분하지 못하는 제약사항.
            //  401/502/504는 위에서 throw하고, 500은 세션 생성 후 데이터 파싱 실패이므로 보수적으로 null 처리.
            logger.warn { "졸업사정표 조회 실패 (데이터 없음 가능성), null 반환: status=${e.serviceStatusCode}, detail=${e.serviceDetail}" }
            null
        }
    }

    fun syncUsaintData(
        studentId: String,
        sToken: String,
    ): RusaintUsaintDataResponse = runBlocking {
        val academicDeferred = async(Dispatchers.IO) { getAcademicSnapshot(studentId, sToken) }
        delay(500)
        val graduationDeferred = async(Dispatchers.IO) { getGraduationSnapshot(studentId, sToken) }

        val academic = academicDeferred.await()
        val graduation = graduationDeferred.await()

        if (academic == null) {
            throw StudentInfoMappingException(
                validationError = "basic_info_unavailable",
                partialUsaintData = null,
                message = "기본 학적 정보를 조회할 수 없습니다. 직접 정보를 입력해주세요.",
            )
        }

        // 학생 정보 검증 포함 병합
        val mergeResult = rusaintSnapshotMerger.mergeWithValidation(
            academic = academic,
            graduation = graduation,
            studentIdPrefix = studentId.take(4),
        )

        if (mergeResult.validationError != null) {
            throw StudentInfoMappingException(
                validationError = mergeResult.validationError,
                partialUsaintData = mergeResult.data,
            )
        }

        requireNotNull(mergeResult.data) { "mergeResult.data is null despite validation success" }
        mergeResult.data
    }

    /**
     * SSO 토큰 유효성 검증 (세션 생성만 시도).
     * 콜백 시점에 sToken 만료 여부를 빠르게 확인하는 용도 (약 1-2초).
     *
     * @throws RusaintServiceException 토큰이 유효하지 않거나 만료된 경우
     */
    fun validateToken(
        studentId: String,
        sToken: String,
    ) {
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
        executeRusaintCall("validate-token", studentId.take(4)) {
            restTemplate.postForEntity<Map<String, Any>>(
                "/api/usaint/validate-token",
                requestEntity,
            )
        }
    }

    private fun <T> executeRusaintCall(callName: String, studentIdPrefix: String, block: () -> T): T {
        return try {
            block()
        } catch (e: HttpStatusCodeException) {
            val detail = extractDetail(e)
            val errorMessage = detail ?: e.message ?: "rusaint 서비스 호출 실패"
            logger.error(e) {
                "rusaint-service 호출 실패: call=$callName, status=${e.statusCode.value()}, detail=$detail"
            }
            Notification.notifyRusaintServiceError(
                operation = callName,
                statusCode = e.statusCode.value(),
                errorMessage = errorMessage,
                studentIdPrefix = studentIdPrefix,
            )
            throw RusaintServiceException(
                message = "rusaint 서비스 호출이 실패했습니다. (status=${e.statusCode.value()})",
                serviceStatusCode = e.statusCode.value(),
                serviceDetail = detail,
            )
        } catch (e: RestClientException) {
            val errorMessage = "WAS ↔ rusaint-service 연결 실패: ${e.message}"
            logger.error(e) { "rusaint-service 통신 중 예외 발생: call=$callName" }
            Notification.notifyRusaintServiceError(
                operation = callName,
                statusCode = null,
                errorMessage = errorMessage,
                studentIdPrefix = studentIdPrefix,
            )
            throw RusaintServiceException(
                message = "rusaint 서비스와 통신할 수 없습니다. (${e.message})",
            )
        }
    }

    private fun extractDetail(e: HttpStatusCodeException): String? {
        return try {
            val body = e.responseBodyAsString
            if (body.isBlank()) return null
            val map: Map<String, Any?> = jacksonObjectMapper().readValue(body)
            map["detail"]?.toString()
        } catch (_: Exception) {
            null
        }
    }
}
