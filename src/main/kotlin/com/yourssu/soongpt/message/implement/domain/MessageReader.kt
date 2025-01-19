package com.yourssu.soongpt.message.implement.domain

import org.springframework.stereotype.Component

@Component
class MessageReader(
    private val messageRepository: MessageRepository,
) {
    fun find(id: Long): Message {
        return messageRepository.get(id)
    }

    fun findAll(): List<Message> {
        return messageRepository.findAll()
    }
}
