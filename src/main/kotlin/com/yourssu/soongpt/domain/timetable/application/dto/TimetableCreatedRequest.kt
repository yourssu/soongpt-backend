package com.yourssu.soongpt.domain.timetable.application.dto

import com.yourssu.soongpt.domain.timetable.business.dto.TimetableCreatedCommand
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.hibernate.validator.constraints.Range

data class TimetableCreatedRequest(
    @field:Range(min = 15, max = 25, message = "학번은 15부터 25까지 가능합니다.")
    val schoolId: Int,

    @field:NotBlank(message = "학과는 필수 입력값입니다.")
    val department: String,

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,

    val isChapel: Boolean = false,

    @field:NotNull
    val majorRequiredCourses: List<String>,

    @field:NotNull
    val majorElectiveCourses: List<String>,

    @field:NotNull
    val generalRequiredCourses: List<String>,

    @field:Range(min = 0, max = 22, message = "전선 희망 학점은 최대 22학점입니다.")
    val majorElectiveCredit: Int,


    @field:Range(min = 0, max = 22, message = "교선 희망 학점은 최대 22학점입니다.")
    val generalElectiveCredit: Int
) {
    fun toCommand(): TimetableCreatedCommand {
        return TimetableCreatedCommand(
            departmentName = department,
            grade = grade,
            isChapel = isChapel,
            majorRequiredCourses = majorRequiredCourses.distinct(),
            majorElectiveCourses = majorElectiveCourses.distinct(),
            generalRequiredCourses = generalRequiredCourses.distinct(),
            majorElectiveCredit = majorElectiveCredit,
            generalElectiveCredit = generalElectiveCredit,
        )
    }
}
