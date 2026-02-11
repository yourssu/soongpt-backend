package com.yourssu.soongpt.domain.course.implement

/**
 * 교직 대분류 영역
 * - MAJOR: 전공영역 (교과교육영역)
 * - TEACHING: 교직영역 (교직이론영역, 교육실습영역)
 * - SPECIAL: 특성화영역
 */
enum class TeachingMajorArea(
        val displayName: String,
        val fieldValues: List<String>,
) {
    MAJOR("전공영역", listOf("전공영역")),
    TEACHING("교직영역", listOf("교직영역")),
    SPECIAL("특성화영역", listOf("특성화")),
    ;

    companion object {
        fun from(value: String): TeachingMajorArea {
            return when (value.trim()) {
                "전공영역", "MAJOR" -> MAJOR
                "교직영역", "TEACHING" -> TEACHING
                "특성화영역", "SPECIAL" -> SPECIAL
                else -> throw IllegalArgumentException("Invalid teaching major area: $value")
            }
        }
    }
}
