package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.FilterTeachingCoursesQuery
import com.yourssu.soongpt.domain.course.implement.TeachingArea
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class GetTeachingCoursesRequest(
    @field:ValidSchoolId
    val schoolId: Int,

    @field:NotBlank
    val department: String,

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,

    val teachingArea: String? = null,
) {
    fun toQuery(): FilterTeachingCoursesQuery {
        val teachingAreaEnum = teachingArea?.let { TeachingArea.from(it) }

        return FilterTeachingCoursesQuery(
            schoolId = schoolId,
            departmentName = department,
            grade = grade,
            teachingArea = teachingAreaEnum,
        )
    }
}
