package com.yourssu.soongpt.domain.contact.implement

interface ContactRepository {
    fun save(contact: Contact): Contact
}
