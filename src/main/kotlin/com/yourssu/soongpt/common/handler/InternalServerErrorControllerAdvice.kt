package com.yourssu.soongpt.common.handler

import com.yourssu.soongpt.common.handler.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.dao.DataAccessResourceFailureException
import org.springframework.http.HttpStatus
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

    @ExceptionHandler(DataAccessResourceFailureException::class)
    fun handleDataAccessException(e: DataAccessResourceFailureException): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(
                ErrorResponse(
                    status = HttpStatus.SERVICE_UNAVAILABLE.value(),
                    message = "데이터베이스에 접근할 수 없습니다. {{ ${e.message} }}"
                )
            )
    }
}