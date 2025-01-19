package com.yourssu.soongpt.message.implement.domain

import org.springframework.stereotype.Component

@Component
class MessageWriter(
    private val messageRepository: MessageRepository,
) {
    fun save(message: Message): Message {
        return messageRepository.save(message)
    }
}