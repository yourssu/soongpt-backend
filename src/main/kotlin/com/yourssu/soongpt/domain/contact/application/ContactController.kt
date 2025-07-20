package com.yourssu.soongpt.domain.contact.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.contact.application.dto.ContactRequest
import com.yourssu.soongpt.domain.contact.business.ContactService
import com.yourssu.soongpt.domain.contact.implement.Contact
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contacts")
class ContactController(
    private val contactService: ContactService
) {
    @PostMapping
    fun createContact(@Valid @RequestBody request: ContactRequest): ResponseEntity<Response<Contact>> {
        val result = contactService.saveContact(request.content)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = result))
    }
}
