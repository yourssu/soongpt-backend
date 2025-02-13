package com.yourssu.soongpt.common.handler

import com.yourssu.soongpt.common.handler.dto.ErrorResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.ConstraintViolationException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.resource.NoResourceFoundException


private val logger = KotlinLogging.logger {}

@ControllerAdvice
class ControllerAdvice {
    companion object {
        private const val VALIDATION_DEFAULT_ERROR_MESSAGE = "Unknown validation error"
        private const val INVALID_REQUEST_DELIMITER = ", "
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.badRequest()
            .body(ErrorResponse.from(e))
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFound(e: NotFoundException): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(e.status)
            .body(ErrorResponse.from(e))
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(e.status)
            .body(ErrorResponse.from(e))
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(e: ForbiddenException): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(e.status)
            .body(ErrorResponse.from(e))
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(e: ConflictException): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(e.status)
            .body(ErrorResponse.from(e))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidException(
        bindingResult: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errorMessage = bindingResult.fieldErrors
            .map { it.defaultMessage ?: VALIDATION_DEFAULT_ERROR_MESSAGE }
            .joinToString(INVALID_REQUEST_DELIMITER) { "Invalid Input: [$it]" }
        logger.error { errorMessage }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = errorMessage
                )
            )
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleValidateException(
        e: ConstraintViolationException
    ): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = e.message ?: VALIDATION_DEFAULT_ERROR_MESSAGE
                )
            )
    }

    @ExceptionHandler(NoResourceFoundException::class)
    fun noResourceFoundException(
        e: NoResourceFoundException
    ): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(e.statusCode)
            .body(
                ErrorResponse(
                    status = e.statusCode.value(),
                    message = "올바르지 않은 경로입니다. {{ /${e.resourcePath} }}"
                )
            )
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDuplicateKeyException(
        e: DataIntegrityViolationException
    ): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(
                ErrorResponse(
                    status = HttpStatus.CONFLICT.value(),
                    message = "중복된 아이디가 존재합니다. {{ ${e.message} }}"
                )
            )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(
        e: HttpRequestMethodNotSupportedException
    ): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(
                ErrorResponse(
                    status = HttpStatus.METHOD_NOT_ALLOWED.value(),
                    message = "허용되지 않은 메소드입니다. {{ ${e.message} }}"
                )
            )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        e: HttpMessageNotReadableException
    ): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "잘못된 RequestBody입니다. {{ ${e.message} }}"
                )
            )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleArgumentTypeMismatchException(
        e: MethodArgumentTypeMismatchException
    ): ResponseEntity<ErrorResponse> {
        logger.error { e }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    status = HttpStatus.BAD_REQUEST.value(),
                    message = "잘못된 타입의 요청입니다. {{ ${e.message} }}"
                )
            )
    }
}

abstract class Error(
    val status: HttpStatus,
    override val message: String
) : Exception()

open class InternalServerError(
    status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
    message: String = "알 수 없는 에러가 발생했습니다."
) : Error(status, message) {
}

open class BadRequestException(
    status: HttpStatus = HttpStatus.BAD_REQUEST,
    message: String = "잘못된 요청입니다.",
) : Error(status, message) {
}

open class NotFoundException(
    status: HttpStatus = HttpStatus.NOT_FOUND,
    message: String = "존재하지 않는 리소스입니다.",
) : Error(status, message) {
}

open class UnauthorizedException(
    status: HttpStatus = HttpStatus.UNAUTHORIZED,
    message: String = "인증되지 않은 사용자입니다.",
) : Error(status, message) {
}

open class ForbiddenException(
    status: HttpStatus = HttpStatus.FORBIDDEN,
    message: String = "권한이 없습니다.",
) : Error(status, message) {
}

open class ConflictException(
    status: HttpStatus = HttpStatus.CONFLICT,
    message: String = "이미 존재하는 리소스입니다.",
) : Error(status, message) {
}
