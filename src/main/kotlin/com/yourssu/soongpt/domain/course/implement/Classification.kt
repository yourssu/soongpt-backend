package com.yourssu.soongpt.domain.course.implement

enum class Classification(
    val displayName: String,
) {
    MAJOR_REQUIRED("전필"),
    MAJOR_ELECTIVE("전선"),
    GENERAL_REQUIRED("교필"),
    GENERAL_ELECTIVE("교선"),
    CHAPEL("채플");
}
