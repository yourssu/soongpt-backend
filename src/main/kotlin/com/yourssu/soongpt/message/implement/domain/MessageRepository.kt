package com.yourssu.soongpt.message.implement.domain

interface MessageRepository {
    fun save(message: Message): Message
    fun get(id: Long): Message
    fun findAll(): List<Message>
}
