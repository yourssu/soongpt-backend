package com.yourssu.soongpt.message.business

import com.yourssu.soongpt.message.implement.Message

class MessageCreatedCommand(
    val content: String
) {
    fun toDomain(): Message {
        return Message(content = content)
    }
}
