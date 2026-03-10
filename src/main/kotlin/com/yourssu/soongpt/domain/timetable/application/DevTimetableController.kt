package com.yourssu.soongpt.domain.timetable.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.timetable.business.TimetableService
import com.yourssu.soongpt.domain.timetable.business.dto.LabTimetableResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "LAB", description = "리크루팅/과제용 lab 전용 API (추후 삭제 예정)")
@RestController
@RequestMapping("/api/dev")
class DevTimetableController(
    private val timetableService: TimetableService,
) {
    @Operation(summary = "랜덤 시간표 조회 (lab)", description = "채플 포함, 싸강 제외, 태그 다양화된 랜덤 시간표 1건 반환. 인증 불필요.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = LabTimetableResponse::class))
                ]
            ),
            ApiResponse(responseCode = "404", description = "유효한 시간표 없음", content = [])
        ]
    )
    @GetMapping("/timetables")
    fun getRandomTimetable(): ResponseEntity<Response<LabTimetableResponse>> {
        val response = timetableService.getRandomLabTimetable()
        return ResponseEntity.ok(Response(result = response))
    }
}
