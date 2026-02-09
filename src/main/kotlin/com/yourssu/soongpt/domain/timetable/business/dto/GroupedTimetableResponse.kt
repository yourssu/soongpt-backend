package com.yourssu.soongpt.domain.timetable.business.dto

data class GroupedTimetableResponse(
    val tag: String,
    val recommendations: List<RecommendationDto>
)
