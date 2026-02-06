package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.timetable.implement.dto.TimetableCandidate

/**
 * 최종 랭킹을 매기기 전, 생성된 제안 후보를 임시로 담는 데이터 클래스
 */
data class SuggestionCandidate(
    val resultingTimetableCandidate: TimetableCandidate,
    val description: String,
)
