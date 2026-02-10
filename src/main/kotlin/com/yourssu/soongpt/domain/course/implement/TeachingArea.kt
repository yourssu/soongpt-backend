package com.yourssu.soongpt.domain.course.implement

enum class TeachingArea(
    val displayName: String,
    val keywords: List<String>,
) {
    THEORY("교직이론", listOf("교육학개론", "교육철학", "교육과정", "교육방법", "교육심리", "교육사회", "교육평가", "교육행정", "생활지도")),
    LITERACY("교직소양", listOf("특수교육학개론", "교직실무", "학교폭력예방")),
    PRACTICE("교육실습", listOf("학교현장실습", "교육봉사", "교육실습")),
    SUBJECT_EDUCATION("교과교육", listOf("교과교육론", "논리및논술")),
    ;

    companion object {
        fun from(value: String): TeachingArea {
            return when (value.trim()) {
                "교직이론", "THEORY" -> THEORY
                "교직소양", "LITERACY" -> LITERACY
                "교육실습", "PRACTICE" -> PRACTICE
                "교과교육", "교과교육영역", "SUBJECT_EDUCATION" -> SUBJECT_EDUCATION
                else -> throw IllegalArgumentException("Invalid teaching area: $value")
            }
        }

        /**
         * 과목명을 기반으로 교직 영역 판별
         */
        fun matchByCourseName(courseName: String): TeachingArea? {
            return entries.firstOrNull { area ->
                area.keywords.any { keyword -> courseName.contains(keyword) }
            }
        }
    }
}
