package com.yourssu.soongpt.domain.course.implement.dto

data class PageableInfo(
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val page: Int,
) {
}
