package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.course.implement.Course

enum class CourseFixture(
    val courseName: String,
    val professorName: String,
    val classification: Classification,
    val credit: Int,
) {
    MAJOR_REQUIRED(
        courseName = "전공필수",
        professorName = "교수명",
        classification = Classification.MAJOR_REQUIRED,
        credit = 3,
    ),
    MAJOR_ELECTIVE(
        courseName = "전공선택",
        professorName = "교수명",
        classification = Classification.MAJOR_ELECTIVE,
        credit = 3,
    ),
    GENERAL_REQUIRED(
        courseName = "교양필수",
        professorName = "교수명",
        classification = Classification.GENERAL_REQUIRED,
        credit = 3,
    )
    ;

    fun toDomain(): Course {
        return Course(
            courseName = courseName,
            professorName = professorName,
            classification = classification,
            credit = credit,
        )
    }
}