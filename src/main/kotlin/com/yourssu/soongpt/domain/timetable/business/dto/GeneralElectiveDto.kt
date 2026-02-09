package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "영역별 수강 가능 교양 과목 그룹")
data class GeneralElectiveDto(
    @Schema(description = "교양 영역 이름", example = "핵심역량-창의")
    val trackName: String,

    @Schema(description = "해당 영역에서 수강 가능한 과목 목록")
    val courses: List<TimetableCourseResponse>
)
