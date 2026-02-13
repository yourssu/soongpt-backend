package com.yourssu.soongpt.domain.timetable.business.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "교양 이수 진행 상황. required/completed 센티널: -2=제공 불가(recommend/all과 동일)")
data class GeneralElectiveProgress(
    @Schema(description = "필수 학점. 제공 불가 시 -2", example = "9")
    val required: Int,

    @Schema(description = "이수 학점. 제공 불가 시 -2", example = "10")
    val completed: Int,

    @Schema(description = "이수 완료 여부", example = "true")
    val satisfied: Boolean,

    @Schema(description = "분야별 이수 학점", example = "{\"인간·언어\": 3, \"문화\": 3}")
    val fieldCredits: Map<String, Int>,
)

@Schema(description = "수강 가능한 교양 과목 목록 응답")
data class AvailableGeneralElectivesResponse(
    @Schema(description = "교양 이수 진행 상황. 항상 존재. rusaint/졸업사정 없으면 required/completed=-2, satisfied=false, fieldCredits=빈 맵")
    val progress: GeneralElectiveProgress,

    @Schema(description = "영역별 수강 가능한 교양 과목 목록")
    val courses: List<GeneralElectiveDto>,
)
