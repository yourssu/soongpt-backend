package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * rusaint-service 관련 설정.
 *
 * - baseUrl: rusaint-service의 기본 URL
 * - pseudonymSecret: pseudonym 생성을 위한 HMAC 시크릿 키
 */
@ConfigurationProperties(prefix = "rusaint")
data class RusaintProperties(
    val baseUrl: String,
    val pseudonymSecret: String,
)
