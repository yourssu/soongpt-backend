package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.dto.PageableInfo
import org.springframework.data.domain.Page

data class SearchCoursesResponse(
    val content: List<SearchCourseResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
) {
    companion object {
        fun from(
            courses: List<Course>,
            pageableInfo: PageableInfo,
        ): SearchCoursesResponse {
            val content = courses.map { SearchCourseResponse.from(it) }
            return SearchCoursesResponse(
                content = content,
                totalElements = pageableInfo.totalElements,
                totalPages = pageableInfo.totalPages,
                size = pageableInfo.size,
                number = pageableInfo.page,
            )
        }
        
        fun from(page: Page<Course>): SearchCoursesResponse {
            val content = page.content.map { SearchCourseResponse.from(it) }
            return SearchCoursesResponse(
                content = content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                size = page.size,
                number = page.number,
            )
        }
    }
}
