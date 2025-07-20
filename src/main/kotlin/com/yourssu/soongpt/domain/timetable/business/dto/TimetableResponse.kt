package com.yourssu.soongpt.domain.timetable.business.dto

data class TimetableResponse(
    val timetableId: Long,
    val tag: String,
    val score: Int = 0,
    val totalPoint: Double = 0.0,
    val courses: List<TimetableCourseResponse>,
) {
    companion object {
//        fun from(timetable: Timetable, courses: List<TimetableCourseResponse>): TimetableResponse {
//            return TimetableResponse(
//                timetableId = timetable.timetableId,
//                tag = timetable.tag,
//                score = timetable.score,
//                totalPoint = timetable.totalPoint,
//                courses = courses,
//            )
//        }
    }
}
