package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery

data class SearchCoursesRequest(
    val q: String = "",
    val page: Int = 0,
    val size: Int = 20,
    val sort: String = "ASC"
) {
    fun toQuery(): SearchCoursesQuery {
        return SearchCoursesQuery(
            query = q,
            page = page,
            size = size,
            sort = sort
        )
    }
}
