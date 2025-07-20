package com.yourssu.soongpt.domain.timetable.business.dto

class TimetableCreatedCommand(
    val departmentName: String,
    val grade: Int,
    val isChapel: Boolean,
    val majorRequiredCodes: List<Long>,
    val majorElectiveCodes: List<Long>,
    val generalRequiredCodes: List<Long>,
    val codes: List<Long>,
    val generalElectivePoint: Int,
) {
}
