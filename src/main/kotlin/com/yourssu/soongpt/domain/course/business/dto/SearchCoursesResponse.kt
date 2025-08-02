package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import org.springframework.data.domain.Page

data class SearchCoursesResponse(
    val content: List<SearchCourseResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int,
) {
    companion object {
        fun from(page: Page<Course>): SearchCoursesResponse {
            val content = page.content.map { SearchCourseResponse.from(it) }
            return SearchCoursesResponse(
                content = content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                size = page.size,
                page = page.number,
            )
        }
    }
}
