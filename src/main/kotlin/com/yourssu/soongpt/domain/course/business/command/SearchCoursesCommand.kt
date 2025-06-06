package com.yourssu.soongpt.domain.course.business.command

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable

class SearchCoursesCommand(
    val schoolId: Int,
    val department: String,
    val grade: Int,
    val q: String,
    val page: Int,
    val size: Int,
    val sort: String,
) {
    fun toPageable(): Pageable {
        return PageRequest.of(page, size, org.springframework.data.domain.Sort.by(sort))
    }

    fun query(): String {
        return q
    }
}
