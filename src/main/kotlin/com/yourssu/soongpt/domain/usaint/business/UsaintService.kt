package com.yourssu.soongpt.domain.usaint.business

import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.business.dto.UsaintSyncResponse
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsaintService(
    private val pseudonymGenerator: PseudonymGenerator,
    private val rusaintServiceClient: RusaintServiceClient,
) {

    private val logger = KotlinLogging.logger {}

    /**
     * u-saint 데이터 동기화 플로우의 진입점.
     *
     * 1) 학번 기반 pseudonym 생성 (WAS 측 검증/캐시 키용)
     * 2) rusaint-service two-track 호출 (academic → 0.5초 후 graduation) 후 병합
     * 3) rusaint 응답의 pseudonym·과목코드 등은 그대로 사용 (과목코드로 조회 시 사용)
     */
    @Transactional
    fun syncUsaintData(
        request: UsaintSyncRequest,
    ): UsaintSyncResponse {
        val pseudonym = pseudonymGenerator.generate(request.studentId)

        val usaintSnapshot = rusaintServiceClient.syncUsaintData(
            studentId = request.studentId,
            sToken = request.sToken,
        )

        logger.info { "유세인트 데이터 동기화 완료: pseudonym=$pseudonym, rusaint.pseudonym=${usaintSnapshot.pseudonym}" }

        return UsaintSyncResponse(
            summary = "usaint data synced",
        )
    }
}
