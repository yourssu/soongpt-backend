package com.yourssu.soongpt.common.application.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Response<T>(
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")),
    val result: T,
)