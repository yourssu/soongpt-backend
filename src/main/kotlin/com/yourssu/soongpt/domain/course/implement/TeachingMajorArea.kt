package com.yourssu.soongpt.domain.course.implement

/**
 * 교직 이수 관점의 대분류
 * - 전공영역: 교과교육영역 등
 * - 교직영역: 교직이론/교직소양/교육실습
 * - 특성화: 교직 과정 특성화 과목
 */
enum class TeachingMajorArea(
    val displayName: String,
    val fieldPrefix: String,
) {
    MAJOR("전공영역", "전공영역/"),
    TEACHING("교직영역", "교직영역/"),
    SPECIALIZATION("특성화", "특성화/"),
    ;

    companion object {
        fun from(value: String): TeachingMajorArea {
            return when (value.trim()) {
                "전공영역", "MAJOR" -> MAJOR
                "교직영역", "TEACHING" -> TEACHING
                "특성화", "SPECIALIZATION", "SPECIAL" -> SPECIALIZATION
                else -> throw IllegalArgumentException("Invalid teaching major area: $value")
            }
        }
    }
}
