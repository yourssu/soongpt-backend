package com.yourssu.soongpt.domain.sso.implement

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
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
        val existing = cache.getIfPresent(pseudonym)
        val session = if (existing?.usaintData != null) {
            existing.copy(status = SyncStatus.PROCESSING, updatedAt = Instant.now())
        } else {
            SyncSession(pseudonym = pseudonym, status = SyncStatus.PROCESSING)
        }
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
        val mergedSummary = mergeGraduationSummary(existing.graduationSummary, new.graduationSummary, pseudonym)

        val keepGraduationReqs = new.graduationRequirements == null && existing.graduationRequirements != null
        if (keepGraduationReqs) {
            logger.warn {
                "새 graduationRequirements null → 기존 값 유지: pseudonym=${pseudonym.take(8)}..."
            }
        }
        return new.copy(
            graduationRequirements = new.graduationRequirements ?: existing.graduationRequirements,
            graduationSummary = mergedSummary,
        )
    }

    /**
     * graduationSummary 필드 단위 merge.
     * 기존에 유효한 값이 있으면 새 값이 null이거나 0,0,true(유세인트 None→0 변환 결과)일 때 기존 값 유지.
     */
    private fun mergeGraduationSummary(
        existing: RusaintGraduationSummaryDto?,
        new: RusaintGraduationSummaryDto?,
        pseudonym: String,
    ): RusaintGraduationSummaryDto? {
        if (new == null) return existing
        if (existing == null) return new

        fun pick(field: String, old: RusaintCreditSummaryItemDto?, fresh: RusaintCreditSummaryItemDto?): RusaintCreditSummaryItemDto? {
            if (old != null && !old.isEmptyData() && (fresh == null || fresh.isEmptyData())) {
                logger.warn { "[merge] $field: 새 값이 null/0,0,true → 기존 값 유지 (pseudonym=${pseudonym.take(8)}..., 기존=${old.required}/${old.completed})" }
                return old
            }
            return fresh ?: old
        }

        return new.copy(
            generalRequired = pick("generalRequired", existing.generalRequired, new.generalRequired),
            generalElective = pick("generalElective", existing.generalElective, new.generalElective),
            majorFoundation = pick("majorFoundation", existing.majorFoundation, new.majorFoundation),
            majorRequired = pick("majorRequired", existing.majorRequired, new.majorRequired),
            majorElective = pick("majorElective", existing.majorElective, new.majorElective),
            minor = pick("minor", existing.minor, new.minor),
            doubleMajorRequired = pick("doubleMajorRequired", existing.doubleMajorRequired, new.doubleMajorRequired),
            doubleMajorElective = pick("doubleMajorElective", existing.doubleMajorElective, new.doubleMajorElective),
            christianCourses = pick("christianCourses", existing.christianCourses, new.christianCourses),
            chapel = new.chapel ?: existing.chapel,
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
