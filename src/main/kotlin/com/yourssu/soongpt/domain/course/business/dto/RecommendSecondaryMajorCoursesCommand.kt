package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType

data class RecommendSecondaryMajorCoursesCommand(
    val departmentName: String,
    val grade: Int,
    val trackType: SecondaryMajorTrackType,
    val completionType: SecondaryMajorCompletionType,
    val takenSubjectCodes: List<String> = emptyList(),
    val progress: String? = null,
    val satisfied: Boolean = false,
)
