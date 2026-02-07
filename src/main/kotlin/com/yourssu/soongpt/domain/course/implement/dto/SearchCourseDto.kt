package com.yourssu.soongpt.domain.course.implement.dto

import com.yourssu.soongpt.domain.course.implement.Course

data class SearchCourseDto(
    val content: List<Course>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int,
) {
    companion object {
        fun from(
            courses: List<Course>,
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
    }
}
