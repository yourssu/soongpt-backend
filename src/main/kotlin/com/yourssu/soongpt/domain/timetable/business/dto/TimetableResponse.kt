package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.timetable.implement.Timetable

data class TimetableResponse(
    val timetableId: Long,
    val tag: String,
    var score: Int?,
    val courses: List<TimetableCourseResponse>,
) {
    companion object {
        fun from(timetable: Timetable, courses: List<TimetableCourseResponse>, score: Int? = null): TimetableResponse {
            return TimetableResponse(
                timetableId = timetable.id!!,
                tag = timetable.tag.name,
                score = score,
                courses = courses,
            )
        }
    }
}