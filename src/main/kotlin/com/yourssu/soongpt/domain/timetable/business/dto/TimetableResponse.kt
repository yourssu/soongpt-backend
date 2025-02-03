package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.timetable.implement.Timetable

data class TimetableResponse(
    val timetableId: Long,
    val tag: String,
    val courses: List<TimetableCourseResponse>,
) {
    companion object {
        fun from(timetable: Timetable, courses: List<TimetableCourseResponse>): TimetableResponse {
            return TimetableResponse(
                timetableId = timetable.id!!,
                tag = timetable.tag.name,
                courses = courses,
            )
        }
    }
}