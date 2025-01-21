package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.message.business.MessageCreatedCommand
import com.yourssu.soongpt.message.implement.Message

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
