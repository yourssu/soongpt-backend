package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.FilterTeachingCoursesQuery
import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea
import jakarta.validation.constraints.NotBlank

data class GetTeachingCoursesRequest(
        @field:ValidSchoolId val schoolId: Int,
        @field:NotBlank val department: String,
        val majorArea: String? = null,
) {
    fun toQuery(): FilterTeachingCoursesQuery {
        val majorAreaEnum = majorArea?.let { TeachingMajorArea.from(it) }

        return FilterTeachingCoursesQuery(
                schoolId = schoolId,
                departmentName = department,
                majorArea = majorAreaEnum,
        )
    }
}
