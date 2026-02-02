package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.config.InternalJwtIssuer
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintSyncRequest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

/**
 * rusaint-service 호출용 HTTP 요청 엔티티 생성.
 * 단일 책임: JWT 발급 + Authorization 헤더 + 요청 바디 구성.
 */
@Component
class RusaintRequestBuilder(
    private val internalJwtIssuer: InternalJwtIssuer,
) {

    fun buildRequestEntity(studentId: String, sToken: String): HttpEntity<*> {
        val headers = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            setBearerAuth(internalJwtIssuer.issueToken())
        }
        val body = RusaintSyncRequest(studentId = studentId, sToken = sToken)
        return HttpEntity(body, headers)
    }
}
