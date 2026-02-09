package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.exception.InvalidCategoryException
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "전공 과목 추천 요청")
data class RecommendMajorCoursesRequest(
    @field:Schema(
        description = "추천할 카테고리 필터 (선택). 지정하지 않으면 전공기초/전공필수/전공선택 모두 조회. 콤마 구분으로 여러 개 지정 가능",
        example = "MAJOR_REQUIRED",
        allowableValues = ["MAJOR_BASIC", "MAJOR_REQUIRED", "MAJOR_ELECTIVE", "RETAKE"],
        nullable = true
    )
    val category: String? = null, // MAJOR_BASIC, MAJOR_REQUIRED, MAJOR_ELECTIVE, RETAKE (콤마 구분 가능)
) {
    companion object {
        private const val RETAKE = "RETAKE"
    }

    fun toCategories(): List<Category> {
        if (category.isNullOrBlank()) {
            return listOf(Category.MAJOR_BASIC, Category.MAJOR_REQUIRED, Category.MAJOR_ELECTIVE)
        }

        return category.split(",")
            .map { it.trim() }
            .filter { it != RETAKE }
            .map { categoryStr ->
                try {
                    Category.valueOf(categoryStr)
                } catch (e: IllegalArgumentException) {
                    throw InvalidCategoryException()
                }
            }
            .filter { it == Category.MAJOR_BASIC || it == Category.MAJOR_REQUIRED || it == Category.MAJOR_ELECTIVE }
    }

    fun includesRetake(): Boolean {
        if (category.isNullOrBlank()) return false
        return category.split(",").map { it.trim() }.any { it == RETAKE }
    }
}
