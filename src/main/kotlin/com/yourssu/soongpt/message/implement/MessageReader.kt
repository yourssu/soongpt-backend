package com.yourssu.soongpt.message.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class MessageReader(
    private val messageRepository: MessageRepository,
) {
    @Transactional(readOnly = true)
    fun find(id: Long): Message {
        return messageRepository.get(id)
    }

    @Transactional(readOnly = true)
    fun findAll(): List<Message> {
        return messageRepository.findAll()
    }
}
