package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.config.SlackProperties
import com.yourssu.soongpt.common.infrastructure.dto.SlackAlarmRequest
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class SlackAlarmProducer(
    private val slackAlarmFeignClient: SlackAlarmFeignClient,
    private val slackProperties: SlackProperties,
) {
    @Async
    fun sendAlarm(message: String) {
        try {
            slackAlarmFeignClient.sendAlarm(SlackAlarmRequest(channel = slackProperties.channelId, text = message))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}