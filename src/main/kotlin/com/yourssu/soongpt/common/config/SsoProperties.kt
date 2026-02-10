package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * SSO 및 클라이언트 인증 관련 설정.
 *
 * - frontendUrl: 프론트엔드 URL (SSO 콜백 후 리다이렉트 대상)
 * - clientJwtSecret: 클라이언트용 JWT 서명/검증 시크릿 (최소 32바이트)
 * - sessionTtlMinutes: 동기화 세션 TTL (분). 기본 60분.
 * - jwtValidityMinutes: 클라이언트 JWT 유효기간 (분). 기본 60분.
 */
@ConfigurationProperties(prefix = "sso")
data class SsoProperties(
    val frontendUrl: String,
    val clientJwtSecret: String,
    val allowedRedirectUrls: List<String> = emptyList(),
    val sessionTtlMinutes: Long = 60L,
    val jwtValidityMinutes: Long = 60L,
    val cookieSameSite: String = "Lax",
)
