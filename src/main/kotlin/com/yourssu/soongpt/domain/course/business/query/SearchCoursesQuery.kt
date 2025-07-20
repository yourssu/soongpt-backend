package com.yourssu.soongpt.domain.course.business.query

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class SearchCoursesQuery(
    val schoolId: Int,
    val department: String,
    val grade: Int,
    val query: String,
    val page: Int,
    val size: Int,
    val sort: String,
) {
    fun toPageable(): Pageable {
        return PageRequest.of(page, size, Sort.by(sort))
    }

    fun query(): String {
        return query
    }
}
