package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack")
class SlackProperties(
    val channelId: String,
)