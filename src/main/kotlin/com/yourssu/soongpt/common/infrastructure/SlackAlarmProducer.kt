package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.config.SlackProperties
import com.yourssu.soongpt.common.infrastructure.dto.SlackAlarmRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class SlackAlarmProducer(
    private val slackAlarmFeignClient: SlackAlarmFeignClient,
    private val slackProperties: SlackProperties,
) {
    @Async("threadPoolTaskExecutor")
    fun sendAlarm(message: String) {
        try {
            logger.info { "Sending alarm to slack!!" }
            slackAlarmFeignClient.sendAlarm(SlackAlarmRequest(channel = slackProperties.channelId, text = message))
            logger.info { "Sent alarm to slack!!" }
        } catch (e: Exception) {
            logger.error { "Failed to send alarm to slack!! exception: $e" }
        }
    }
}