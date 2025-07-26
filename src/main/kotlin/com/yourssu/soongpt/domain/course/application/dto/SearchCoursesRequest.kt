package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Range

data class SearchCoursesRequest(
    @field:Pattern(regexp = "^[a-zA-Z0-9가-힣\\s._-]*$", message = "검색어는 한글, 영문, 숫자, 공백, 점, 언더스코어, 하이픈만 사용 가능합니다")
    @field:Range(min = 2, max = 100, message = "검색어는 2자 이상 100자 이하이어야 합니다")
    val q: String,
    
    @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    val page: Int = 0,
    
    @field:Range(min = 1, max = 100, message = "페이지 크기는 1 이상 100이하 이어야 합니다")
    val size: Int = 20,

    @field:Pattern(regexp = "^(?i)(ASC|DESC)$", message = "정렬 방식은 ASC 또는 DESC만 허용됩니다 (대소문자 구분 없음)")
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
