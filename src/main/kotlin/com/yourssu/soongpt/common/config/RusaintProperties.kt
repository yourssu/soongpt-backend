package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * rusaint-service 관련 설정.
 *
 * - baseUrl: rusaint-service의 기본 URL
 * - pseudonymSecret: pseudonym 생성을 위한 HMAC 시크릿 키
 * - internalJwtSecret: WAS ↔ rusaint-service 내부 JWT 서명/검증용 시크릿
 * - internalJwtValidityMinutes: 내부 JWT 유효기간(분). 기본 15분.
 */
@ConfigurationProperties(prefix = "rusaint")
data class RusaintProperties(
    val baseUrl: String,
    val pseudonymSecret: String,
    val internalJwtSecret: String,
    val internalJwtValidityMinutes: Long = 15L,
)
