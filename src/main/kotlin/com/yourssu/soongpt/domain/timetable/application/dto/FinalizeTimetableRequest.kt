package com.yourssu.soongpt.domain.timetable.application.dto

import com.yourssu.soongpt.domain.timetable.business.dto.FinalizeTimetableCommand
import jakarta.validation.constraints.NotNull

data class FinalizeTimetableRequest(
    @field:NotNull(message = "기반이 되는 시간표 ID는 필수입니다.")
    val timetableId: Long,
    val generalElectiveCourseCodes: List<Long> = emptyList(),
    val chapelCourseCode: Long? = null
) {
    fun toCommand(): FinalizeTimetableCommand {
        return FinalizeTimetableCommand(
            timetableId = timetableId,
            generalElectiveCourseCodes = generalElectiveCourseCodes,
            chapelCourseCode = chapelCourseCode
        )
    }
}
