package com.yourssu.soongpt.message.application.dto

import com.yourssu.soongpt.message.business.MessageCreatedCommand
import jakarta.validation.constraints.NotBlank

data class MessageCreateRequest(
    @NotBlank
    val content: String,
) {
    fun toCommand(): MessageCreatedCommand {
        return MessageCreatedCommand(
            content = content
        )
    }
}