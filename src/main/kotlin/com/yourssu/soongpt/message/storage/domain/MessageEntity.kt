package com.yourssu.soongpt.message.storage.domain

import com.yourssu.soongpt.message.implement.domain.Message
import jakarta.persistence.*

@Table(name = "message")
@Entity
class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    val content: String,
) {
    companion object {
        fun from(message: Message): MessageEntity {
            return MessageEntity(
                id = message.id,
                content = message.content,
            )
        }
    }

    fun toDomain(): Message {
        return Message(
            id = id,
            content = content,
        )
    }
}
