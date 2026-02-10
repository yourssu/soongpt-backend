package com.yourssu.soongpt.domain.course.business.query

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

class SearchCoursesQuery(
    val query: String,
    val page: Int,
    val size: Int,
    val sort: String,
) {
    fun toPageable(): Pageable {
        val direction = Sort.Direction.fromString(sort)
        return PageRequest.of(page, size, Sort.by(direction, "name"))
    }

    fun query(): String {
        return query
    }
}
