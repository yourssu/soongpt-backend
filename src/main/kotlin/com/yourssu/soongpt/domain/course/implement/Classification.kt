package com.yourssu.soongpt.domain.course.implement

enum class Classification(
    val displayName: String,
) {
    MAJOR_CORE("전필"),
    MAJOR_ELECTIVE("전선"),
    GENERAL_CORE("교필"),
    GENERAL_ELECTIVE("교선"),
    CHAPEL("채플");
}
