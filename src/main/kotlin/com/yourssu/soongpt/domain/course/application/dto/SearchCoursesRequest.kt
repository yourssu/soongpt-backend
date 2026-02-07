package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import org.hibernate.validator.constraints.Range

private const val MAX_LENGTH = 50

data class SearchCoursesRequest(
    val q: String = "",
    
    @field:Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    val page: Int = 0,
    
    @field:Range(min = 1, max = 100, message = "페이지 크기는 1 이상 100이하 이어야 합니다")
    val size: Int = 20,

    @field:Pattern(regexp = "^(?i)(ASC|DESC)$", message = "정렬 방식은 ASC 또는 DESC만 허용됩니다 (대소문자 구분 없음)")
    val sort: String = "ASC"
) {
    fun toQuery(): SearchCoursesQuery {
        return SearchCoursesQuery(
            query = sanitizeQuery(q),
            page = page,
            size = size,
            sort = sort
        )
    }

    private fun sanitizeQuery(input: String): String {
        return ALLOWED_CHARS_REGEX.findAll(input.take(MAX_LENGTH))
            .map { it.value }
            .joinToString("")
            .trim()
    }

    companion object {
        private const val MAX_LENGTH = 50
        private val ALLOWED_CHARS_REGEX = """[가-힣a-zA-ZⅠ-Ⅹ0-9\s\n().,;:_-]""".toRegex()
    }
}
