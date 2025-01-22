package com.yourssu.soongpt.common.handler.dto

import com.yourssu.soongpt.common.handler.Error
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ErrorResponse(
    val timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYY-MM-dd HH:mm:ss")),
    val status: Int,
    val message: String,
) {
    companion object {
        fun from(e: Error): ErrorResponse {
            return ErrorResponse(
                status = e.status.value(),
                message = e.message,
            )
        }
    }

}