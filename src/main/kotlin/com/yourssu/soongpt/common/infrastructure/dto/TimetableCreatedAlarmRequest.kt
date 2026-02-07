package com.yourssu.soongpt.common.infrastructure.dto

class TimetableCreatedAlarmRequest(
    val schoolId: Int,
    val departmentName: String,
    val times: Long,
) {
    companion object {
        fun from(schoolId: Int, departmentName: String, times: Long): TimetableCreatedAlarmRequest {
            return TimetableCreatedAlarmRequest(schoolId, departmentName, times)
        }
    }
}