package com.yourssu.soongpt.domain.timetable.implement

import org.springframework.stereotype.Component

@Component
class TimetableReader (
    private val timetableRepository: TimetableRepository
) {
    fun get(id: Long): Timetable {
        return timetableRepository.get(id)
    }

    fun count(): Long {
        return timetableRepository.count()
    }

    fun getRandom(): Timetable? {
        return timetableRepository.findRandom()
    }

    fun getRandomByTag(tag: Tag): Timetable? {
        return timetableRepository.findRandomByTag(tag)
    }
}
