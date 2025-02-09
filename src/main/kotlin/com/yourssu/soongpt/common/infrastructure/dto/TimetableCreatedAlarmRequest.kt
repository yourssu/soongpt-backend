package com.yourssu.soongpt.common.infrastructure.dto

class TimetableCreatedAlarmRequest(
    val schoolId: Int,
    val departmentName: String,
    val times: Int,
) {
    companion object {
        fun from(schoolId: Int, departmentName: String, times: Int): TimetableCreatedAlarmRequest {
            return TimetableCreatedAlarmRequest(schoolId, departmentName, times)
        }
    }
}