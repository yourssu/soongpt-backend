package com.yourssu.soongpt.message.business

import com.yourssu.soongpt.message.business.dto.MessageResponse
import com.yourssu.soongpt.message.implement.MessageReader
import com.yourssu.soongpt.message.implement.MessageWriter
import org.springframework.stereotype.Service

@Service
class MessageService(
    private val messageWriter: MessageWriter,
    private val messageReader: MessageReader,
) {
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

