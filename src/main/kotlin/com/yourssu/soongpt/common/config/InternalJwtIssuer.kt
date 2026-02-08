package com.yourssu.soongpt.common.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * WAS ↔ rusaint-service 간 내부 JWT 발급.
 * rusaint-service의 verify_internal_jwt에서 동일 시크릿으로 검증합니다.
 * HS256 사용, 시크릿은 최소 32바이트 권장.
 * 유효기간은 rusaint.internal-jwt-validity-minutes 로 설정 (기본 15분).
 */
@Component
class InternalJwtIssuer(
    private val rusaintProperties: RusaintProperties,
) {
    init {
        val secretSize = rusaintProperties.internalJwtSecret.toByteArray(Charsets.UTF_8).size
        require(secretSize >= KEY_MIN_BYTES) {
            "Internal JWT secret (rusaint.internal-jwt-secret) must be at least $KEY_MIN_BYTES bytes (current: $secretSize). " +
                "Please check RUSAINT_INTERNAL_JWT_SECRET environment variable."
        }
    }

    fun issueToken(): String {
        val secretBytes = rusaintProperties.internalJwtSecret.toByteArray(Charsets.UTF_8)
        val key: SecretKey = Keys.hmacShaKeyFor(secretBytes)
        val now = System.currentTimeMillis()
        val validityMs = rusaintProperties.internalJwtValidityMinutes * 60 * 1000
        val exp = now + validityMs

        return Jwts.builder()
            .subject(SUBJECT)
            .issuer(ISSUER)
            .issuedAt(Date(now))
            .expiration(Date(exp))
            .signWith(key)
            .compact()
    }

    companion object {
        private const val SUBJECT = "usaint-sync"
        private const val ISSUER = "soongpt-backend"
        private const val KEY_MIN_BYTES = 32 // HS256 최소 키 길이
    }
}
