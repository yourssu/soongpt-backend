package com.yourssu.soongpt.domain.timetable.implement

import org.springframework.stereotype.Component


@Component
class TimetableWriter (
    private val timetableRepository: TimetableRepository
) {
    fun save(timetable: Timetable): Timetable {
        return timetableRepository.save(timetable)
    }

    fun delete(id: Long) {
        timetableRepository.delete(id)
    }
}