package com.yourssu.soongpt.domain.timetable.business

import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponse
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TimetableService(
) {
    @Transactional
    fun createTimetable(command: TimetableCreatedCommand): TimetableResponses {
        return TimetableResponses(listOf())
    }

    fun getTimeTable(id: Long): TimetableResponse {
        return TimetableResponse(
            timetableId = id,
            tag = "Sample Tag",
            score = null,
            totalPoint = 0,
            courses = listOf()
        )
    }
}
