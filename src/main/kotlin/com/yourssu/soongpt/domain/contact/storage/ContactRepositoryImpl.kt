package com.yourssu.soongpt.domain.contact.storage

import com.yourssu.soongpt.domain.contact.implement.Contact
import com.yourssu.soongpt.domain.contact.implement.ContactRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class ContactRepositoryImpl(
    private val contactJpaRepository: ContactJpaRepository,
) : ContactRepository {
    override fun save(contact: Contact): Contact {
        return contactJpaRepository.save(ContactEntity.from(contact)).toDomain()
    }
}

interface ContactJpaRepository : JpaRepository<ContactEntity, Long>
