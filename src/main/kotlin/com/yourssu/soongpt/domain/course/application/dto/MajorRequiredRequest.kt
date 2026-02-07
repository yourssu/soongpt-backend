package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.MajorRequiredCourseQuery
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class MajorRequiredRequest(
    @field:ValidSchoolId
    val schoolId: Int,

    @field:NotBlank
    val department: String,

    val subDepartment: String? = null,

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,
) {
    fun toQuery(): MajorRequiredCourseQuery {
        return MajorRequiredCourseQuery(
            departmentName = department,
            subDepartmentName = subDepartment,
            grade = grade,
            schoolId = schoolId
        )
    }
}
