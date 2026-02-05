package com.yourssu.soongpt.domain.sso.implement

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * 동기화 세션을 Caffeine 캐시에 저장하는 저장소.
 * TTL은 sso.session-ttl-minutes로 설정 (기본 60분).
 * 키는 pseudonym.
 */
@Component
class SyncSessionStore(
    ssoProperties: SsoProperties,
) {
    private val logger = KotlinLogging.logger {}

    private val cache: Cache<String, SyncSession> = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(ssoProperties.sessionTtlMinutes))
        .maximumSize(10_000) // 최대 1만 세션
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

    fun updateStatus(pseudonym: String, status: SyncStatus, usaintData: RusaintUsaintDataResponse? = null) {
        val existing = cache.getIfPresent(pseudonym) ?: return
        val updated = existing.copy(
            status = status,
            updatedAt = Instant.now(),
            usaintData = usaintData ?: existing.usaintData,
        )
        cache.put(pseudonym, updated)
        logger.info { "세션 상태 변경: pseudonym=${pseudonym.take(8)}..., status=$status" }
    }

    fun getUsaintData(pseudonym: String): RusaintUsaintDataResponse? {
        return cache.getIfPresent(pseudonym)?.usaintData
    }

    fun hasSession(pseudonym: String): Boolean {
        return cache.getIfPresent(pseudonym) != null
    }

    fun size(): Long = cache.estimatedSize()
}
