package com.yourssu.soongpt.domain.timetable.business.dto

data class TimetableCreatedCommand(
    val departmentName: String,
    val grade: Int,
    val isChapel: Boolean,
    val majorRequiredCourses: List<String>,
    val majorElectiveCourses: List<String>,
    val generalRequiredCourses: List<String>,
    val generalElectiveCredit: Int,
) {
}