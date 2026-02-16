package com.yourssu.soongpt.common.filter

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.ContentCachingRequestWrapper
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.StandardCharsets

private val log = KotlinLogging.logger {}
private const val MAX_LOG_BODY_LENGTH = 3000

private val sensitiveHeaderNames = setOf(
    "authorization",
    "cookie",
    "set-cookie",
    "x-admin-password",
    "x-api-key",
    "api-key",
    "proxy-authorization",
    "x-auth-token",
    "x-access-token",
    "soongpt_auth",
)

private val sensitivePayloadKeys = setOf(
    "password",
    "new_password",
    "old_password",
    "token",
    "access_token",
    "refresh_token",
    "authorization",
    "id_token",
    "code",
    "soongpt_auth",
    "api_key",
    "apikey",
    "secret",
)

@Component
class LoggingFilter : OncePerRequestFilter() {
    private val objectMapper = ObjectMapper()

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val requestWrapper = ContentCachingRequestWrapper(request)
        val responseWrapper = ContentCachingResponseWrapper(response)

        val startTime = System.currentTimeMillis()
        filterChain.doFilter(requestWrapper, responseWrapper)
        val duration = System.currentTimeMillis() - startTime

        val method = requestWrapper.method
        val requestUri = requestWrapper.requestURI
        val headers = requestWrapper.headerNames.toList()
            .associateWith {
                sanitizeHeader(
                    it,
                    requestWrapper.getHeader(it),
                )
            }
            .entries.joinToString(", ") { "\"${it.key}\"=\"${it.value}\"" }
        val requestPayload = sanitizePayload(
            String(requestWrapper.contentAsByteArray, StandardCharsets.UTF_8),
            requestWrapper.contentType,
        )
        val responseStatus = responseWrapper.status
        val responsePayload = sanitizePayload(
            String(responseWrapper.contentAsByteArray, StandardCharsets.UTF_8),
            responseWrapper.contentType,
        )
        log.info {
            """{"Request":{"Method":"$method $requestUri - ${duration}ms","Payload":${objectMapper.writeValueAsString(requestPayload)},"Headers": {$headers}}, "Reply":{"Payload":${objectMapper.writeValueAsString(responsePayload)}}"""
                .replace("\n", "")
        }
        log.info {
            """{"Reply":{"Method":"$method $requestUri - ${duration}ms","Status":$responseStatus}}"""
                .replace("\n", "")
        }
        responseWrapper.copyBodyToResponse()
    }

    private fun sanitizeHeader(name: String, value: String?): String {
        if (value == null) return ""
        return if (sensitiveHeaderNames.contains(name.lowercase())) {
            "[REDACTED]"
        } else {
            value
        }
    }

    private fun sanitizePayload(payload: String, contentType: String?): String {
        val rawPayload = payload.ifEmpty { "{}" }
        if (!isProbablyJson(contentType, rawPayload)) {
            return truncate(rawPayload)
        }

        return sanitizeJsonPayload(rawPayload)
    }

    private fun isProbablyJson(contentType: String?, payload: String): Boolean {
        if (contentType?.lowercase()?.contains("json") == true) {
            return true
        }

        val trimmed = payload.trimStart()
        return trimmed.startsWith("{") || trimmed.startsWith("[")
    }

    private fun sanitizeJsonPayload(payload: String): String {
        return runCatching {
            val sanitized = sanitizeJsonNode(objectMapper.readTree(payload))
            objectMapper.writeValueAsString(sanitized)
        }.getOrElse {
            truncate(payload)
        }
    }

    private fun sanitizeJsonNode(node: JsonNode): JsonNode {
        return when {
            node.isObject -> {
                val objectNode = node.deepCopy<ObjectNode>()
                val fields = objectNode.fields()
                while (fields.hasNext()) {
                    val (fieldName, fieldValue) = fields.next()
                    if (isSensitivePayloadKey(fieldName)) {
                        objectNode.set<JsonNode>(fieldName, objectMapper.valueToTree("[REDACTED]"))
                    } else {
                        objectNode.set(fieldName, sanitizeJsonNode(fieldValue))
                    }
                }
                objectNode
            }
            node.isArray -> {
                val arrayNode = node.deepCopy<ArrayNode>()
                for (i in 0 until arrayNode.size()) {
                    arrayNode.set(i, sanitizeJsonNode(arrayNode[i]))
                }
                arrayNode
            }
            else -> node
        }
    }

    private fun isSensitivePayloadKey(fieldName: String): Boolean {
        return sensitivePayloadKeys.contains(fieldName.lowercase())
    }

    private fun truncate(payload: String): String {
        return if (payload.length > MAX_LOG_BODY_LENGTH) {
            payload.take(MAX_LOG_BODY_LENGTH) + "...(truncated)"
        } else {
            payload
        }
    }
}
