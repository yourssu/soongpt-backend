package com.yourssu.soongpt.common.infrastructure

import com.yourssu.soongpt.common.infrastructure.dto.SlackAlarmRequest
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Component
@FeignClient(
    name = "slackAlarmClient",
)
interface SlackAlarmFeignClient {
    @PostMapping
    fun sendAlarm(@RequestBody request: SlackAlarmRequest)
}