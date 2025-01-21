package com.yourssu.soongpt.message.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MessageWriter(
    private val messageRepository: MessageRepository,
) {
    @Transactional
    fun save(message: Message): Message {
        return messageRepository.save(message)
    }
}