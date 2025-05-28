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
                "전필", "전기" -> MAJOR_REQUIRED
                "전선" -> MAJOR_ELECTIVE
                "교필" -> GENERAL_REQUIRED
                "교선" -> GENERAL_ELECTIVE
                "채플" -> CHAPEL
                else -> null
            }
        }
    }
}
