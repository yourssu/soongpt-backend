package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.course.implement.Course

enum class CourseFixture(
    val courseName: String,
    val professorName: String,
    val classification: Classification,
    val courseCode: Int,
    val credit: Int,
) {
    MAJOR_REQUIRED(
        courseName = "전공필수",
        professorName = "교수명",
        classification = Classification.MAJOR_REQUIRED,
        courseCode = 1,
        credit = 3,
    ),
    MAJOR_ELECTIVE(
        courseName = "전공선택",
        professorName = "교수명",
        classification = Classification.MAJOR_ELECTIVE,
        courseCode = 2,
        credit = 3,
    ),
    GENERAL_REQUIRED(
        courseName = "교양필수",
        professorName = "교수명",
        classification = Classification.GENERAL_REQUIRED,
        courseCode = 3,
        credit = 3,
    ),
    ;

    fun toDomain(courseCode: Int = this.courseCode): Course {
        return Course(
            courseName = courseName,
            professorName = professorName,
            classification = classification,
            courseCode = courseCode,
            credit = credit,
        )
    }

    fun toDomainRandomCourseCode() : Course {
        return Course(
            courseName = courseName,
            professorName = professorName,
            classification = classification,
            courseCode = (Math.random() * 100000).toInt(),
            credit = credit,
        )
    }
}