package com.yourssu.soongpt.domain.timetable.business.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class FinalTimetableRecommendationResponse(
    val status: RecommendationStatus,
    val successResponse: List<GroupedTimetableResponse>? = null,
    val singleConflictCourses: List<DeletableCourseDto>? = null
) {
    companion object {
        fun success(response: List<GroupedTimetableResponse>) = FinalTimetableRecommendationResponse(
            status = RecommendationStatus.SUCCESS,
            successResponse = response
        )

        fun singleConflict(courses: List<DeletableCourseDto>) = FinalTimetableRecommendationResponse(
            status = RecommendationStatus.SINGLE_CONFLICT,
            singleConflictCourses = courses
        )

        fun failure() = FinalTimetableRecommendationResponse(
            status = RecommendationStatus.FAILURE
        )
    }
}
