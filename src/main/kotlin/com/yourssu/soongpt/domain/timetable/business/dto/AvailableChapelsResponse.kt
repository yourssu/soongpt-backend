package com.yourssu.soongpt.domain.timetable.business.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "채플 이수 진행 상황")
data class ChapelProgress(
    @Schema(description = "채플 이수 완료 여부", example = "true")
    val satisfied: Boolean,
)

@Schema(description = "수강 가능한 채플 과목 목록 응답")
data class AvailableChapelsResponse(
    @Schema(description = "채플 이수 진행 상황 (rusaint 없으면 null)")
    val progress: ChapelProgress?,

    @Schema(description = "수강 가능한 채플 과목 목록")
    val courses: List<TimetableCourseResponse>,
)
