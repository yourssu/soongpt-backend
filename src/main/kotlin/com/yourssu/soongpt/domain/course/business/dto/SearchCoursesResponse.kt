package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course2
import com.yourssu.soongpt.domain.course.implement.dto.PageableInfo

data class SearchCoursesResponse(
    val content: List<SearchCourseResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int,
) {
    companion object {
        fun from(
            courses: List<Course2>,
            pageableInfo: PageableInfo,
        ): SearchCoursesResponse {
            val content = courses.map { SearchCourseResponse.from(it) }
            return SearchCoursesResponse(
                content = content,
                totalElements = pageableInfo.totalElements,
                totalPages = pageableInfo.totalPages,
                size = pageableInfo.size,
                page = pageableInfo.page,
            )
        }
    }
}
