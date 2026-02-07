package com.yourssu.soongpt.domain.timetable.business.dto

import com.yourssu.soongpt.domain.timetable.implement.Timetable

data class TimetableResponse(
    val timetableId: Long,
    val tag: String,
    val score: Int? = 0,
    val totalPoint: Double = 0.0,
    val courses: List<TimetableCourseResponse>,
) {
    companion object {
        fun from(timetable: Timetable, courses: List<TimetableCourseResponse>): TimetableResponse {
            return TimetableResponse(
                timetableId = timetable.id!!,
                tag = timetable.tag.description,
                score = timetable.score,
                totalPoint = courses.sumOf { it.point.toDouble() },
                courses = courses,
            )
        }
    }
}
