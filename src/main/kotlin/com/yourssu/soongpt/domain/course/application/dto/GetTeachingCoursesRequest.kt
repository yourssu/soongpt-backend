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

    val teachingArea: String? = null,
) {
    fun toQuery(): FilterTeachingCoursesQuery {
        val teachingAreaEnum = teachingArea?.let { TeachingArea.from(it) }

        return FilterTeachingCoursesQuery(
            schoolId = schoolId,
            departmentName = department,
            teachingArea = teachingAreaEnum,
        )
    }
}
