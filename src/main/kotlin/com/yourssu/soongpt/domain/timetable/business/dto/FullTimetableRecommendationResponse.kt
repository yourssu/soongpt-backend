package com.yourssu.soongpt.domain.timetable.business.dto

/**
 * 1차 추천과 대안 추천을 모두 포함하는 통합 응답 DTO
 */
data class FullTimetableRecommendationResponse(
    val primaryTimetable: TimetableResponse,
    val alternativeSuggestions: List<RecommendationDto>
)
