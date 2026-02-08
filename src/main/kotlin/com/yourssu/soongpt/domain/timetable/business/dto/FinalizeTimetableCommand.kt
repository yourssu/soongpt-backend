package com.yourssu.soongpt.domain.timetable.business.dto

data class FinalizeTimetableCommand(
    val timetableId: Long,
    val generalElectiveCourseCodes: List<Long>,
    val chapelCourseCode: Long?
)
