package com.yourssu.soongpt.domain.timetable.implement

import org.springframework.stereotype.Component

@Component
class TimetableReader(
    private val timetableRepository: TimetableRepository,
) {
    fun get(id: Long): Timetable {
        return timetableRepository.get(id)
    }
}