package com.yourssu.soongpt.domain.course.application.dto

import io.swagger.v3.oas.annotations.media.Schema

/**
 * 통합 과목 추천에서 사용하는 이수구분 카테고리
 */
enum class RecommendCategory(val displayName: String) {
    MAJOR_BASIC("전공기초"),
    MAJOR_REQUIRED("전공필수"),
    MAJOR_ELECTIVE("전공선택"),
    GENERAL_REQUIRED("교양필수"),
    GENERAL_ELECTIVE("교양선택"),
    RETAKE("재수강"),
    DOUBLE_MAJOR("복수전공"),
    MINOR("부전공"),
    TEACHING("교직이수"),
}

@Schema(description = "통합 과목 추천 요청")
data class RecommendCoursesRequest(
    @field:Schema(
        description = "추천할 이수구분 필터. 콤마 구분으로 여러 개 지정 가능",
        example = "MAJOR_REQUIRED,MAJOR_ELECTIVE",
        required = true,
    )
    val category: String,
) {
    fun toCategories(): List<RecommendCategory> {
        return category.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map { categoryStr ->
                try {
                    RecommendCategory.valueOf(categoryStr)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("Invalid recommend category: $categoryStr")
                }
            }
    }
}
