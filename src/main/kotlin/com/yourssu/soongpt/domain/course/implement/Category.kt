package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.InvalidCategoryException

enum class Category(
    val displayName: String,
    val group: String,
) {
    MAJOR_REQUIRED("전필", "MAJOR"),
    MAJOR_ELECTIVE("전선", "MAJOR"),
    MAJOR_BASIC("전기", "MAJOR"),
    GENERAL_REQUIRED("교필", "GENERAL"),
    GENERAL_ELECTIVE("교선", "GENERAL"),
    CHAPEL("채플", "OTHER"),
    TEACHING("교직", "OTHER"),
    OTHER("기타", "OTHER"),
    ;

    companion object {
        fun from(name: String): Category? {
            return when (name) {
                "전필" -> MAJOR_REQUIRED
                "전선" -> MAJOR_ELECTIVE
                "전기" -> MAJOR_BASIC
                "교필" -> GENERAL_REQUIRED
                "교선" -> GENERAL_ELECTIVE
                "채플" -> CHAPEL
                "교직" -> TEACHING
                else -> throw InvalidCategoryException()
            }
        }

        fun match(category: String): Category {
            return Category.entries.find { category.contains(it.displayName) }
                ?: OTHER
        }
    }
}
