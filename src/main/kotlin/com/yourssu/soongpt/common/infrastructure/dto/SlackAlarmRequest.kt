package com.yourssu.soongpt.common.infrastructure.dto

data class SlackAlarmRequest(
    val channel: String,
    val text: String,
)
