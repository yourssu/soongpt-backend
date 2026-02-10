package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.business.query.FilterCoursesByTrackQuery
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class GetCoursesByTrackRequest(
    @field:ValidSchoolId
    val schoolId: Int,

    @field:NotBlank
    val department: String,

    @field:NotBlank
    val trackType: String,

    val completionType: String? = null,
) {
    fun toQuery(): FilterCoursesByTrackQuery {
        val trackTypeEnum = SecondaryMajorTrackType.from(trackType)
        val completionTypeEnum = completionType?.let {
            SecondaryMajorCompletionType.from(it)
        }

        return FilterCoursesByTrackQuery(
            schoolId = schoolId,
            departmentName = department,
            trackType = trackTypeEnum,
            completionType = completionTypeEnum,
        )
    }
}
