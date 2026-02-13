package com.yourssu.soongpt.domain.timetable.business.dto

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "교양 이수 진행 상황. required/completed 센티널: -2=제공 불가(recommend/all과 동일)")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GeneralElectiveProgress(
    @Schema(description = "필수 학점. 제공 불가 시 -2", example = "9")
    val required: Int,

    @Schema(description = "이수 학점. 제공 불가 시 -2", example = "10")
    val completed: Int,

    @Schema(description = "이수 완료 여부", example = "true")
    val satisfied: Boolean,

    @Schema(
        description = "분야별 이수 과목 수. 20학번: 창의·융합역량(세부필드 객체). 21~22: 균형교양교과(세부필드 객체). 23~: 인간/문화/사회/과학/자기개발(Int). 19학번 이하·제공 불가: 필드 생략(required/completed/satisfied만)",
        example = "{\"균형교양교과\": {\"문학·예술\": 1, \"역사·철학·종교\": 0, \"정치·경제·경영\": 2, \"사회·문화·심리\": 0, \"자연과학·공학·기술\": 0}, \"숭실품성교과\": 0}",
        nullable = true
    )
    val fieldCredits: Map<String, Any>?,
)

@Schema(description = "수강 가능한 교양 과목 목록 응답")
data class AvailableGeneralElectivesResponse(
    @Schema(description = "교양 이수 진행 상황. 항상 존재. rusaint/졸업사정 없으면 required/completed=-2, satisfied=false, fieldCredits 생략")
    val progress: GeneralElectiveProgress,

    @Schema(description = "영역별 수강 가능한 교양 과목 목록")
    val courses: List<GeneralElectiveDto>,
)
