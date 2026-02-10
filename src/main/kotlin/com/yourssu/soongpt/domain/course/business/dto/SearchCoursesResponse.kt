package com.yourssu.soongpt.domain.course.business.dto

data class SearchCoursesResponse(
    val courses: List<SearchCourseGroupResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int,
)
