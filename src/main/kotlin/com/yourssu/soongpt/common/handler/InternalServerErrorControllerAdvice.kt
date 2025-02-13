package com.yourssu.soongpt.common.handler

import com.yourssu.soongpt.common.handler.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class InternalServerErrorControllerAdvice {

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.internalServerError()
            .body(ErrorResponse.from(InternalServerError()))
    }
}