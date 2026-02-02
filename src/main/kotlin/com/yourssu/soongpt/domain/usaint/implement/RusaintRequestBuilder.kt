package com.yourssu.soongpt.domain.usaint.implement

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.yourssu.soongpt.common.config.InternalJwtIssuer
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintSyncRequest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets

/** rusaint-service 호출용 HTTP 요청 엔티티. JWT + 본문(JSON) 구성. */
@Component
class RusaintRequestBuilder(
    private val internalJwtIssuer: InternalJwtIssuer,
) {
    private val jsonMapper: ObjectMapper = jacksonObjectMapper()

    fun buildRequestEntity(studentId: String, sToken: String): HttpEntity<String> {
        val body = RusaintSyncRequest(studentId = studentId, sToken = sToken)
        val jsonBody = jsonMapper.writeValueAsString(body)
        val bodyBytes = jsonBody.toByteArray(StandardCharsets.UTF_8)

        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            contentLength = bodyBytes.size.toLong()
            set("Connection", "close")
            setBearerAuth(internalJwtIssuer.issueToken())
        }
        return HttpEntity(jsonBody, headers)
    }
}
