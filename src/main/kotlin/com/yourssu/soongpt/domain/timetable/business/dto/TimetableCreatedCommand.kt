package com.yourssu.soongpt.domain.timetable.business.dto

class TimetableCreatedCommand(
    val departmentName: String,
    val grade: Int,
    val isChapel: Boolean,
    val majorRequiredCourses: List<Long>,
    val majorElectiveCourses: List<Long>,
    val generalRequiredCourses: List<Long>,
    val majorElectiveCredit: Int,
    val generalElectiveCredit: Int,
) {
}
