package com.yourssu.soongpt.message.storage.domain

import com.yourssu.soongpt.common.application.domain.common.NotFoundException
import com.yourssu.soongpt.message.implement.domain.Message
import com.yourssu.soongpt.message.implement.domain.MessageRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class MessageRepositoryImpl(
    private val messageJpaRepository: MessageJpaRepository,
) : MessageRepository {
    override fun save(message: Message): Message {
        return messageJpaRepository.save(MessageEntity.from(message))
            .toDomain()
    }

    override fun get(id: Long): Message {
        return messageJpaRepository.findById(id)
            .orElseThrow { MessageNotFoundException() }
            .toDomain()
    }

    override fun findAll(): List<Message> {
        return messageJpaRepository.findAll()
            .map { it.toDomain() }
    }
}

interface MessageJpaRepository : JpaRepository<MessageEntity, Long> {
}

class MessageNotFoundException : NotFoundException(message = "해당하는 메세지가 없습니다.")
