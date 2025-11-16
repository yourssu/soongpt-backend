package com.yourssu.soongpt.domain.usaint.business

import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.business.dto.UsaintSyncResponse
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsaintService(
    private val pseudonymGenerator: PseudonymGenerator,
    private val rusaintServiceClient: RusaintServiceClient,
) {

    /**
     * u-saint 데이터 동기화 플로우의 진입점.
     *
     * 1) 학번 기반 pseudonym 생성
     * 2) rusaint-service에 내부 요청 전송
     * 3) (추후) 응답 기반 DB 저장 및 캐싱
     *
     * 이 단계에서는 timetable을 생성하지 않고,
     * u-saint에서 가져온 학사 정보를 저장/정제하는 역할만 수행합니다.
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

        // TODO:
        // 1) usaintSnapshot.takenCourses, flags, availableCredits, basicInfo, remainingCredits를
        //    pseudonym과 함께 usaint 전용 테이블/도메인 모델에 저장
        // 2) 해당 정보를 usaint 전용 테이블/도메인 모델에 저장
        // 3) timetable 생성은 /api/timetables/usaint 에서 별도로 수행

        return UsaintSyncResponse(
            // TODO: 실제 동기화된 학기/과목/학점 요약 정보로 확장
            summary = "usaint data synced",
        )
    }
}
