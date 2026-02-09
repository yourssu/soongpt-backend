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

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,

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
            grade = grade,
            trackType = trackTypeEnum,
            completionType = completionTypeEnum,
        )
    }
}
