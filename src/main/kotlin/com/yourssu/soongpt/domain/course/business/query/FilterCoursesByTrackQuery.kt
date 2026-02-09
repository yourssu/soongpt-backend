package com.yourssu.soongpt.domain.course.business.query

import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType

data class FilterCoursesByTrackQuery(
    val schoolId: Int,
    val departmentName: String,
    val trackType: SecondaryMajorTrackType,
    val completionType: SecondaryMajorCompletionType?,
)
