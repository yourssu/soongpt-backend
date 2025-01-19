package com.yourssu.soongpt.message.business.domain

import com.yourssu.soongpt.message.implement.domain.MessageReader
import com.yourssu.soongpt.message.implement.domain.MessageWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class MessageService(
    private val messageWriter: MessageWriter,
    private val messageReader: MessageReader,
) {
    @Transactional
    fun create(command: MessageCreatedCommand): MessageResponse {
        val message = messageWriter.save(command.toDomain())
        return MessageResponse.from(message)
    }

    fun find(messageId: Long) : MessageResponse {
        val message = messageReader.find(messageId)
        return MessageResponse.from(message)
    }

    fun findAll(): List<MessageResponse> {
        return messageReader.findAll().map { MessageResponse.from(it) }
    }
}

