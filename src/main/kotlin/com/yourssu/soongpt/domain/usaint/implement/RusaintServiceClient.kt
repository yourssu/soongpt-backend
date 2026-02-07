package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.config.RusaintProperties
import com.yourssu.soongpt.common.infrastructure.exception.RusaintServiceException
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
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
                    setConnectTimeout(Duration.ofSeconds(3))
                    setReadTimeout(Duration.ofSeconds(8))
                }
                BufferingClientHttpRequestFactory(simple)
            },
        )
        .build()

    fun getAcademicSnapshot(
        studentId: String,
        sToken: String,
    ): RusaintAcademicResponseDto {
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
        return executeRusaintCall("academic") {
            val responseEntity = restTemplate.postForEntity<RusaintAcademicResponseDto>(
                "/api/usaint/snapshot/academic",
                requestEntity,
            )
            requireNotNull(responseEntity.body) { "Empty response from rusaint-service (academic)" }
        }
    }

    fun getGraduationSnapshot(
        studentId: String,
        sToken: String,
    ): RusaintGraduationResponseDto {
        val requestEntity = rusaintRequestBuilder.buildRequestEntity(studentId, sToken)
        return executeRusaintCall("graduation") {
            val responseEntity = restTemplate.postForEntity<RusaintGraduationResponseDto>(
                "/api/usaint/snapshot/graduation",
                requestEntity,
            )
            requireNotNull(responseEntity.body) { "Empty response from rusaint-service (graduation)" }
        }
    }

    fun syncUsaintData(
        studentId: String,
        sToken: String,
    ): RusaintUsaintDataResponse = runBlocking {
        val academicDeferred = async(Dispatchers.IO) { getAcademicSnapshot(studentId, sToken) }
        delay(500)
        val graduationDeferred = async(Dispatchers.IO) { getGraduationSnapshot(studentId, sToken) }
        rusaintSnapshotMerger.merge(
            academicDeferred.await(),
            graduationDeferred.await(),
        )
    }

    private fun <T> executeRusaintCall(callName: String, block: () -> T): T {
        return try {
            block()
        } catch (e: HttpStatusCodeException) {
            logger.error(e) { "rusaint-service 호출 실패: call=$callName, status=${e.statusCode.value()}" }
            throw RusaintServiceException(
                message = "rusaint 서비스 호출이 실패했습니다. (status=${e.statusCode.value()})",
            )
        } catch (e: RestClientException) {
            logger.error(e) { "rusaint-service 통신 중 예외 발생: call=$callName" }
            throw RusaintServiceException(
                message = "rusaint 서비스와 통신할 수 없습니다. (${e.message})",
            )
        }
    }
}
