package com.yourssu.soongpt.common.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

/**
 * 클라이언트 인증용 JWT 발급/검증.
 * HttpOnly 쿠키로 전달되며, pseudonym을 claim에 포함합니다.
 */
@Component
class ClientJwtProvider(
    private val ssoProperties: SsoProperties,
) {
    init {
        val secretSize = ssoProperties.clientJwtSecret.toByteArray(Charsets.UTF_8).size
        require(secretSize >= KEY_MIN_BYTES) {
            "SSO client JWT secret must be at least $KEY_MIN_BYTES bytes (현재: $secretSize)"
        }
    }

    private val key: SecretKey by lazy {
        Keys.hmacShaKeyFor(ssoProperties.clientJwtSecret.toByteArray(Charsets.UTF_8))
    }

    fun issueToken(pseudonym: String): String {
        val now = System.currentTimeMillis()
        val validityMs = ssoProperties.jwtValidityMinutes * 60 * 1000
        val exp = now + validityMs

        return Jwts.builder()
            .subject(pseudonym)
            .issuer(ISSUER)
            .claim(CLAIM_PSEUDONYM, pseudonym)
            .issuedAt(Date(now))
            .expiration(Date(exp))
            .signWith(key)
            .compact()
    }

    fun createAuthCookie(token: String): Cookie {
        return Cookie(COOKIE_NAME, token).apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = (ssoProperties.jwtValidityMinutes * 60).toInt()
            setAttribute("SameSite", ssoProperties.cookieSameSite)
        }
    }

    fun createLogoutCookie(): Cookie {
        return Cookie(COOKIE_NAME, "").apply {
            isHttpOnly = true
            secure = true
            path = "/"
            maxAge = 0
            setAttribute("SameSite", ssoProperties.cookieSameSite)
        }
    }

    fun extractTokenFromRequest(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == COOKIE_NAME }?.value
    }

    fun validateAndGetPseudonym(token: String): Result<String> {
        return try {
            val claims: Claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .payload

            val pseudonym = claims[CLAIM_PSEUDONYM, String::class.java]
                ?: return Result.failure(JwtException("Missing pseudonym claim"))

            Result.success(pseudonym)
        } catch (e: ExpiredJwtException) {
            Result.failure(TokenExpiredException("JWT token expired"))
        } catch (e: JwtException) {
            Result.failure(InvalidTokenException("Invalid JWT token: ${e.message}"))
        }
    }

    fun extractPseudonymFromRequest(request: HttpServletRequest): Result<String> {
        val token = extractTokenFromRequest(request)
            ?: return Result.failure(InvalidTokenException("No auth cookie found"))
        return validateAndGetPseudonym(token)
    }

    companion object {
        const val COOKIE_NAME = "soongpt_auth"
        private const val ISSUER = "soongpt-backend"
        private const val CLAIM_PSEUDONYM = "pseudonym"
        private const val KEY_MIN_BYTES = 32
    }
}

class TokenExpiredException(message: String) : RuntimeException(message)
class InvalidTokenException(message: String) : RuntimeException(message)
