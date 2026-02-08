package com.yourssu.soongpt.domain.timetable.application.dto

import com.yourssu.soongpt.domain.timetable.business.dto.PrimaryTimetableCommand
import com.yourssu.soongpt.domain.timetable.business.dto.SelectedCourseCommand
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Range

data class PrimaryTimetableRequest(
        // 해당 정보를 받아올건지? 내부에서 긁어올건지.
        @field:Range(min = 0, max = 24, message = "최대 학점은 0에서 24 사이여야 합니다.") val maxCredit: Int,
        @field:Valid val retakeCourses: List<SelectedCourseDto> = emptyList(),
        @field:Valid val majorRequiredCourses: List<SelectedCourseDto> = emptyList(),
        @field:Valid val majorElectiveCourses: List<SelectedCourseDto> = emptyList(),
        @field:Valid val otherMajorCourses: List<SelectedCourseDto> = emptyList(),
        @field:Valid val generalRequiredCourses: List<SelectedCourseDto> = emptyList(),
        @field:Valid val addedCourses: List<SelectedCourseDto> = emptyList()
) {
    fun toCommand(): PrimaryTimetableCommand {
        return PrimaryTimetableCommand(
                maxCredit = maxCredit,
                retakeCourses = retakeCourses.map { it.toCommand() },
                majorRequiredCourses = majorRequiredCourses.map { it.toCommand() },
                majorElectiveCourses = majorElectiveCourses.map { it.toCommand() },
                otherMajorCourses = otherMajorCourses.map { it.toCommand() },
                generalRequiredCourses = generalRequiredCourses.map { it.toCommand() },
                addedCourses = addedCourses.map { it.toCommand() }
        )
    }
}

data class SelectedCourseDto(
        @field:NotNull(message = "과목 코드는 필수입니다.") val courseCode: Long,
        @field:NotNull(message = "선택한 분반 ID 목록은 필수입니다.")
        val selectedCourseIds: List<Long> = emptyList()
) {
    fun toCommand(): SelectedCourseCommand {
        return SelectedCourseCommand(courseCode = courseCode, selectedCourseIds = selectedCourseIds)
    }
}
