package com.yourssu.soongpt.domain.contact.business

import com.yourssu.soongpt.domain.contact.implement.Contact
import com.yourssu.soongpt.domain.contact.implement.ContactRepository
import org.springframework.stereotype.Service

@Service
class ContactService(
    private val contactRepository: ContactRepository
) {
    fun saveContact(content: String): Contact {
        val contact = Contact(content = content)
        return contactRepository.save(contact)
    }
}
