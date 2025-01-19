package com.yourssu.soongpt.message.business.domain

import com.yourssu.soongpt.message.implement.domain.Message

class MessageCreateCommand(
    val content: String
) {
    fun toDomain(): Message {
        return Message(content = content)
    }
}
