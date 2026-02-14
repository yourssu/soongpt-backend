package com.yourssu.soongpt.domain.sso.implement

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

@Component
class SyncSessionStore(
    ssoProperties: SsoProperties,
) {
    private val logger = KotlinLogging.logger {}

    private val cache: Cache<String, SyncSession> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(ssoProperties.sessionTtlMinutes))
        .maximumSize(10_000)
        .build()

    fun createSession(pseudonym: String): SyncSession {
        val session = SyncSession(
            pseudonym = pseudonym,
            status = SyncStatus.PROCESSING,
        )
        cache.put(pseudonym, session)
        logger.info { "세션 생성: pseudonym=${pseudonym.take(8)}..." }
        return session
    }

    fun getSession(pseudonym: String): SyncSession? {
        return cache.getIfPresent(pseudonym)
    }

    fun updateStatus(
        pseudonym: String,
        status: SyncStatus,
        usaintData: RusaintUsaintDataResponse? = null,
        failReason: String? = null,
    ) {
        val existing = cache.getIfPresent(pseudonym) ?: return
        val existingData = existing.usaintData
        val finalUsaintData = when {
            usaintData == null -> existingData
            existingData == null -> usaintData
            else -> mergeNeverOverwriteWithNull(existingData, usaintData, pseudonym)
        }
        val updated = existing.copy(
            status = status,
            updatedAt = Instant.now(),
            usaintData = finalUsaintData ?: existingData,
            failReason = failReason,
        )
        cache.put(pseudonym, updated)
        logger.info { "세션 상태 변경: pseudonym=${pseudonym.take(8)}..., status=$status${failReason?.let { ", reason=$it" } ?: ""}" }
    }

    private fun mergeNeverOverwriteWithNull(
        existing: RusaintUsaintDataResponse,
        new: RusaintUsaintDataResponse,
        pseudonym: String,
    ): RusaintUsaintDataResponse {
        val keepGraduationReqs = new.graduationRequirements == null && existing.graduationRequirements != null
        val keepGraduationSummary = new.graduationSummary == null && existing.graduationSummary != null
        if (keepGraduationReqs || keepGraduationSummary) {
            logger.warn {
                "새 usaintData가 null인 필드 있어 기존 값 유지: pseudonym=${pseudonym.take(8)}..., " +
                    "graduationRequirements 유지=$keepGraduationReqs, graduationSummary 유지=$keepGraduationSummary"
            }
        }
        return new.copy(
            graduationRequirements = new.graduationRequirements ?: existing.graduationRequirements,
            graduationSummary = new.graduationSummary ?: existing.graduationSummary,
        )
    }

    fun getUsaintData(pseudonym: String): RusaintUsaintDataResponse? {
        return cache.getIfPresent(pseudonym)?.usaintData
    }

    fun hasSession(pseudonym: String): Boolean {
        return cache.getIfPresent(pseudonym) != null
    }

    fun size(): Long = cache.estimatedSize()
}
