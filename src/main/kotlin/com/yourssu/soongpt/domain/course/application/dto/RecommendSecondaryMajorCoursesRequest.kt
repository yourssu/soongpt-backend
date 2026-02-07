package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.domain.course.business.dto.RecommendSecondaryMajorCoursesCommand
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Range

data class RecommendSecondaryMajorCoursesRequest(
    @field:NotBlank
    val department: String,

    @field:Range(min = 1, max = 5, message = "학년은 1부터 5까지 가능합니다.")
    val grade: Int,

    @field:NotBlank
    val trackType: String,

    @field:NotBlank
    val completionType: String,

    val takenSubjectCodes: List<String> = emptyList(),
    val progress: String? = null,
    val satisfied: Boolean = false,
) {
    fun toCommand(): RecommendSecondaryMajorCoursesCommand {
        val trackTypeEnum = SecondaryMajorTrackType.from(trackType)
        val completionTypeEnum = SecondaryMajorCompletionType.from(completionType)
        SecondaryMajorCompletionType.validateCompatibility(
            trackType = trackTypeEnum,
            completionType = completionTypeEnum,
            rawCompletionType = completionType,
        )

        return RecommendSecondaryMajorCoursesCommand(
            departmentName = department,
            grade = grade,
            trackType = trackTypeEnum,
            completionType = completionTypeEnum,
            takenSubjectCodes = takenSubjectCodes,
            progress = progress,
            satisfied = satisfied,
        )
    }
}
