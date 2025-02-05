package com.yourssu.soongpt.domain.timetable.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.timetable.business.TimetableService
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/timetables")
class TimetableController(
    private val timetableService: TimetableService,
) {
    @GetMapping("/{id}")
    fun getTimetable(@PathVariable id: Long): ResponseEntity<Response<TimetableResponse>> {
        val response = timetableService.getTimeTable(id)
        return ResponseEntity.ok(Response(result=response))
    }
}