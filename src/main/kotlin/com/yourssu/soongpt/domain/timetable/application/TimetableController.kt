package com.yourssu.soongpt.domain.timetable.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.infrastructure.SlackMessageProducer
import com.yourssu.soongpt.common.infrastructure.dto.TimetableCreatedAlarmRequest
import com.yourssu.soongpt.domain.timetable.application.dto.TimetableCreatedRequest
import com.yourssu.soongpt.domain.timetable.business.TimetableService
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

@RestController
@RequestMapping("/api/timetables")
class TimetableController(
    private val timetableService: TimetableService,
    private val slackMessageProducer: SlackMessageProducer,
) {
    @PostMapping
    fun createTimetable(@RequestBody request: TimetableCreatedRequest): ResponseEntity<Response<TimetableResponses>> {
        logger.info { "POST /api/timetables request: ${mapper.writeValueAsString(request)}" }
        val responses = timetableService.createTimetable(request.toCommand())
        slackMessageProducer.sendTimetableCreatedMessage(
            TimetableCreatedAlarmRequest(
                schoolId = request.schoolId,
                departmentName = request.department,
                times = (responses.timetables.lastOrNull()?.timetableId ?: -1L).toInt()
            )
        )
        logger.info { "POST /api/timetables response: ${mapper.writeValueAsString(responses)}" }
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Response(result = responses))
    }


    @GetMapping("/{id}")
    fun getTimetable(@PathVariable id: Long): ResponseEntity<Response<TimetableResponse>> {
        logger.info { "GET /api/timetables/$id request" }
        val response = timetableService.getTimeTable(id)
        return ResponseEntity.ok(Response(result = response))
    }
}