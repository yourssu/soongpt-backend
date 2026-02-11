package com.yourssu.soongpt.domain.timetable.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.timetable.application.dto.FinalizeTimetableRequest
import com.yourssu.soongpt.domain.timetable.application.dto.PrimaryTimetableRequest
import com.yourssu.soongpt.domain.timetable.business.TimetableService
import com.yourssu.soongpt.domain.timetable.business.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Timetable", description = "시간표 관련 API")
@RestController
@RequestMapping("/api/timetables")
class TimetableController(
    private val timetableService: TimetableService,
) {
    @Operation(
        summary = "시간표 추천 및 생성 요청",
        description = """
            사용자가 선택한 과목 목록을 기반으로 시간표 조합을 생성하고, 가능한 제안들을 함께 반환합니다.
            응답의 `status` 필드에 따라 세 가지 경우로 나뉩니다.
            - `SUCCESS`: 조합 생성에 성공했으며, `successResponse`에 최적 시간표와 제안 목록이 담겨있습니다.
            - `SINGLE_CONFLICT`: 조합 생성에 실패했으나, 특정 과목 1개를 제거하면 성공하는 경우입니다. `singleConflictCourses`에 제거 가능한 과목의 정보(`courseCode`, `category`) 리스트가 담겨있습니다.
            - `FAILURE`: 1개 과목 제거로도 조합 생성이 불가능한 경우입니다.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201", description = "요청 성공", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = FinalTimetableRecommendationResponse::class))
                ]
            ),
            ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 조합 실패", content = [])
        ]
    )
    @PostMapping
    fun createTimetable(@RequestBody request: PrimaryTimetableRequest): ResponseEntity<Response<FinalTimetableRecommendationResponse>> {
        val response = timetableService.recommendTimetable(request.toCommand())

        if (response.status == RecommendationStatus.FAILURE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Response(result = response))
        }

        if (response.status == RecommendationStatus.SUCCESS) {
            val slackAlarmRequest = timetableService.createTimetableAlarmRequest(
                response.successResponse!![0].recommendations[0].timetable.timetableId
            )
            Notification.notifyTimetableCreated(slackAlarmRequest)
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Response(result = response))
    }

    @Operation(summary = "최종 시간표 확정", description = "교양/채플 선택 후, 최종 시간표를 확정하여 새로운 시간표로 저장합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "확정 성공", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = TimetableResponse::class))
                ]
            ),
            ApiResponse(responseCode = "400", description = "시간 충돌 발생 또는 잘못된 입력값", content = [])
        ]
    )
    @PostMapping("/finalize")
    fun finalizeTimetable(@RequestBody request: FinalizeTimetableRequest): ResponseEntity<Response<TimetableResponse>> {
        val response = timetableService.finalizeTimetable(request.toCommand())
        val slackAlarmRequest = timetableService.createTimetableAlarmRequest(response.timetableId)
        Notification.notifyTimetableCreated(slackAlarmRequest)
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(summary = "특정 시간표 조회", description = "시간표 ID로 특정 시간표의 상세 정보를 조회합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = TimetableResponse::class))
                ]
            ),
            ApiResponse(responseCode = "404", description = "존재하지 않는 시간표 ID", content = [])
        ]
    )
    @GetMapping("/{id}")
    fun getTimetable(@PathVariable id: Long): ResponseEntity<Response<TimetableResponse>> {
        val response = timetableService.getTimeTable(id)
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(summary = "수강 가능한 교양 과목 목록 조회", description = "특정 시간표를 기준으로, 시간이 겹치지 않는 교양 과목들을 영역별로 그룹화하여 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = GeneralElectiveDto::class))
                ]
            ),
            ApiResponse(responseCode = "404", description = "존재하지 않는 시간표 ID", content = [])
        ]
    )
    @GetMapping("/{id}/available-general-electives")
    fun getAvailableGeneralElectives(@PathVariable id: Long): ResponseEntity<Response<List<GeneralElectiveDto>>> {
        // TODO: 실제 userId는 토큰 등에서 가져와야 함
        val userId = "anonymous"
        val response = timetableService.getAvailableGeneralElectives(id, userId)
        return ResponseEntity.ok(Response(result = response))
    }

    @Operation(summary = "수강 가능한 채플 과목 목록 조회", description = "특정 시간표를 기준으로, 시간이 겹치지 않는 채플 과목 목록을 반환합니다.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200", description = "조회 성공", content = [
                    Content(mediaType = "application/json", schema = Schema(implementation = TimetableCourseResponse::class))
                ]
            ),
            ApiResponse(responseCode = "404", description = "존재하지 않는 시간표 ID", content = [])
        ]
    )
    @GetMapping("/{id}/available-chapels")
    fun getAvailableChapels(@PathVariable id: Long): ResponseEntity<Response<List<TimetableCourseResponse>>> {
        // TODO: 실제 userId는 토큰 등에서 가져와야 함
        val userId = "anonymous"
        val response = timetableService.getAvailableChapels(id, userId)
        return ResponseEntity.ok(Response(result = response))
    }
}
