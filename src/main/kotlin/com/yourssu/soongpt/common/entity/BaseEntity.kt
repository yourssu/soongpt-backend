package com.yourssu.soongpt.common.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@EntityListeners(AuditingEntityListener::class)
@MappedSuperclass
abstract class BaseEntity {
    @CreatedDate
    @Column(name = "create_time", nullable = false, updatable = false)
    lateinit var createdTime: LocalDateTime

    @LastModifiedDate
    @Column(name = "update_time", nullable = false)
    lateinit var updatedTime: LocalDateTime
}