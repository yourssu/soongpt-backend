package com.yourssu.soongpt.domain.timetable.implement

import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import com.yourssu.soongpt.domain.timetable.business.dto.TimetableResponses
import org.springframework.stereotype.Component

@Component
class TimetableGenerator (
){

    fun generate(command: TimetableCreatedCommand): TimetableResponses {
        return TimetableResponses(listOf())
    }

}