package com.yourssu.soongpt.message.business.domain

import com.yourssu.soongpt.message.implement.domain.Message

data class MessageResponse(
    val id: Long,
    val content: String
) {
    companion object {
        fun from(message: Message): MessageResponse {
            return MessageResponse(
                id = message.id!!,
                content = message.content
            )
        }
    }
}