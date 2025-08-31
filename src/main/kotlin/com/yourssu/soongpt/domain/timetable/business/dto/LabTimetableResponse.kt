package com.yourssu.soongpt.domain.timetable.business.dto

data class LabTimetableResponse(
    val timetableId: Long,
    val tag: String,
    val totalCredit: Int,
    val courses: List<LabCourseItem>,
)

data class LabCourseItem(
    val courseName: String,
    val professorName: String?,
    val classification: String,
    val credit: Int,
    val courseTime: List<LabCourseTime>,
)

data class LabCourseTime(
    val week: String,
    val start: String,
    val end: String,
    val classroom: String?,
)
