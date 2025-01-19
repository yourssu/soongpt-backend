package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.message.business.domain.MessageCreatedCommand
import com.yourssu.soongpt.message.implement.domain.Message

enum class MessageFixture(
    val content: String,
) {
    HELLO_WORLD(
        content = "Hello, World!",
    );

    fun toDomain(): Message {
        return Message(content = content)
    }

    fun toCreatedCommand(): MessageCreatedCommand {
        return MessageCreatedCommand(content = content)
    }
}
