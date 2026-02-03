package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.GeneralRequiredCourseQuery
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class GeneralRequiredRequest(
    @field:ValidSchoolId
    val schoolId: Int,

    @field:NotBlank
    val department: String,

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,

    val field: String? = null,
) {
    fun toQuery(): GeneralRequiredCourseQuery {
        return GeneralRequiredCourseQuery(
            departmentName = department,
            grade = grade,
            schoolId = schoolId,
        )
    }
}
