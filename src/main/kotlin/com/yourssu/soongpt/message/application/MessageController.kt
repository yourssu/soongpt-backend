package com.yourssu.soongpt.message.application

import com.yourssu.soongpt.common.application.dto.Response
import com.yourssu.soongpt.message.application.dto.MessageCreateRequest
import com.yourssu.soongpt.message.business.dto.MessageResponse
import com.yourssu.soongpt.message.business.MessageService
import jakarta.validation.Valid
import org.jetbrains.annotations.NotNull
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/messages")
class MessageController(
    private val messageService: MessageService,
) {
    @PostMapping
    fun create(@Valid @RequestBody request: MessageCreateRequest): ResponseEntity<Response<MessageResponse>> {
        val response = messageService.create(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = response))
    }

    @GetMapping("/{messageId}")
    fun find(@PathVariable @NotNull messageId: Long): ResponseEntity<Response<MessageResponse>> {
        val response = messageService.find(messageId)
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping
    fun findAll(): ResponseEntity<Response<List<MessageResponse>>> {
        val response = messageService.findAll()
        return ResponseEntity.ok(Response(result = response))
    }
}

