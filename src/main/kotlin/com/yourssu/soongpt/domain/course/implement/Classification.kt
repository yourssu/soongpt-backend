package com.yourssu.soongpt.domain.course.implement

enum class Classification(
    val displayName: String,
) {
    MAJOR_REQUIRED("전필"),
    MAJOR_ELECTIVE("전선"),
    GENERAL_REQUIRED("교필"),
    GENERAL_ELECTIVE("교선"),
    CHAPEL("채플");

    companion object {
        fun fromName(name: String): Classification? {
            return when (name) {
                "전필", "전기" -> Classification.MAJOR_REQUIRED
                "전선" -> Classification.MAJOR_ELECTIVE
                "교필" -> Classification.GENERAL_REQUIRED
                "교선" -> Classification.GENERAL_ELECTIVE
                "채플" -> Classification.CHAPEL
                else -> null
            }
        }
    }
}