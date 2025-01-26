package com.yourssu.soongpt.domain.course.implement

class Course(
    val id: Long? = null,
    val courseName: String,
    val professorName: String? = null,
    val classification: Classification,
    val courseCode: Int,
    val credit: Int = 0,
) {
}