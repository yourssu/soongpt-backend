package com.yourssu.soongpt.domain.contact.business.dto

import com.yourssu.soongpt.domain.contact.implement.Contact

data class ContactResponse(
    val id: Long? = null,
    val content: String,
) {
    companion object {
        fun from(contact: Contact): ContactResponse {
            return ContactResponse(
                id = contact.id,
                content = contact.content,
            )
        }
    }
}
