package com.yourssu.soongpt.domain.timetable.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.domain.timetable.application.dto.PrimaryTimetableRequest
import com.yourssu.soongpt.domain.timetable.application.dto.TimetableCreatedRequest
import com.yourssu.soongpt.domain.timetable.business.TimetableService
import com.yourssu.soongpt.domain.timetable.business.dto.FullTimetableRecommendationResponse
import com.yourssu.soongpt.domain.timetable.business.dto.LabTimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/timetables")
class TimetableController(
    private val timetableService: TimetableService,
) {
    @PostMapping
    fun createTimetable(@RequestBody request: TimetableCreatedRequest): ResponseEntity<Response<TimetableResponses>> {
        val responses = timetableService.createTimetable(request.toCommand())
        val slackRequest = timetableService.createTimetableAlarmRequest(request, responses)
        Notification.notifyTimetableCreated(slackRequest)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = responses))
    }

    @PostMapping("/full-recommendation")
    fun getFullRecommendation(@RequestBody request: PrimaryTimetableRequest): ResponseEntity<Response<FullTimetableRecommendationResponse>> {
        val response = timetableService.getFullRecommendation(request.toCommand())
        return ResponseEntity.ok(Response(result = response))
    }

    @GetMapping("/{id}")
    fun getTimetable(@PathVariable id: Long): ResponseEntity<Response<TimetableResponse>> {
        val response = timetableService.getTimeTable(id)
        return ResponseEntity.ok(Response(result = response))
    }

    // 웹팀과제를 위한 기능
    @GetMapping
    fun getRandomTimetable(): ResponseEntity<Response<LabTimetableResponse>> {
        val response = timetableService.getRandomTimetable()
        return ResponseEntity.ok(Response(result = response))
    }
}
