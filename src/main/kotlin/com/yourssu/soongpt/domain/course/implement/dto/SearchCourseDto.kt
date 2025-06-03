package com.yourssu.soongpt.domain.course.implement.dto

import com.yourssu.soongpt.domain.course.implement.Course2
import org.springframework.data.domain.Pageable

data class SearchCourseDto(
    val content: List<Course2>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int,
) {
    companion object {
        fun from(
            courses: List<Course2>,
            totalElements: Long,
            totalPages: Int,
            size: Int,
            number: Int,
        ): SearchCourseDto {
            return SearchCourseDto(
                content = courses,
                totalElements = totalElements,
                totalPages = totalPages,
                size = size,
                page = number,
            )
        }

        fun empty(pageable: Pageable): SearchCourseDto {
            return SearchCourseDto(
                content = listOf(),
                totalElements = 0L,
                totalPages = 0,
                size = pageable.pageSize,
                page = pageable.pageNumber,
            )
        }
    }

    fun toPageableInfo(): PageableInfo {
        return PageableInfo(
            totalElements = totalElements,
            totalPages = totalPages,
            size = size,
            page = page,
        )
    }
}
