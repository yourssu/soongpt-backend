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
    MAJOR_CORE(
        courseName = "전공필수",
        professorName = "교수명",
        classification = Classification.MAJOR_CORE,
        courseCode = 1,
        credit = 3,
    );

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