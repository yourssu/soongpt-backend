package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.handler.BadRequestException
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.HttpStatus

/**
 * 통합 과목 추천(/recommend/all)에서 사용하는 이수구분 카테고리.
 * 교양선택은 이 API에서 지원하지 않으며, 별도 API로 제공 예정.
 */
enum class RecommendCategory(val displayName: String) {
    MAJOR_BASIC("전공기초"),
    MAJOR_REQUIRED("전공필수"),
    MAJOR_ELECTIVE("전공선택"),
    GENERAL_REQUIRED("교양필수"),
    RETAKE("재수강"),
    DOUBLE_MAJOR_REQUIRED("복수전공필수"),
    DOUBLE_MAJOR_ELECTIVE("복수전공선택"),
    MINOR("부전공"),
    TEACHING("교직이수"),
}

@Schema(description = "통합 과목 추천 요청")
data class RecommendCoursesRequest(
    @field:Schema(
        description = "추천할 이수구분 필터. 콤마 구분으로 여러 개 지정 가능. 허용값: MAJOR_BASIC, MAJOR_REQUIRED, MAJOR_ELECTIVE, GENERAL_REQUIRED, RETAKE, DOUBLE_MAJOR_REQUIRED, DOUBLE_MAJOR_ELECTIVE, MINOR, TEACHING",
        example = "MAJOR_REQUIRED,MAJOR_ELECTIVE,TEACHING",
        required = true,
        allowableValues = [
            "MAJOR_BASIC",
            "MAJOR_REQUIRED",
            "MAJOR_ELECTIVE",
            "GENERAL_REQUIRED",
            "RETAKE",
            "DOUBLE_MAJOR_REQUIRED",
            "DOUBLE_MAJOR_ELECTIVE",
            "MINOR",
            "TEACHING"
        ],
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
                    throw BadRequestException(
                        status = HttpStatus.BAD_REQUEST,
                        message = "지원하지 않는 이수구분입니다: $categoryStr (교양선택은 이 API에서 사용할 수 없습니다.)",
                    )
                }
            }
    }
}
