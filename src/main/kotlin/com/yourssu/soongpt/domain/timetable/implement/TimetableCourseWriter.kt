package com.yourssu.soongpt.domain.timetable.implement

import org.springframework.stereotype.Component

@Component
class TimetableCourseWriter(
    private val timetableCourseRepository: TimetableCourseRepository,
) {
    fun save(timetableCourse: TimetableCourse): TimetableCourse {
        return timetableCourseRepository.save(timetableCourse)
    }
}