package com.yourssu.soongpt.common.support.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "spring.cors")
data class CorsProperties(
    val allowedOrigins: Array<String>
) {
}
