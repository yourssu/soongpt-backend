package com.yourssu.soongpt.domain.contact.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.contact.implement.Contact
import jakarta.persistence.*

@Entity
@Table(name = "contact")
class ContactEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "content", nullable = false)
    val content: String,
) : BaseEntity() {
    companion object {
        fun from(contact: Contact): ContactEntity {
            return ContactEntity(
                content = contact.content,
            )
        }
    }

    fun toDomain(): Contact {
        return Contact(
            id = id,
            content = content,
        )
    }
}
