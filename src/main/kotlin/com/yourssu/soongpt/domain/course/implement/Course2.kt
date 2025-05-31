package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime

class Course2(
    val id: Long? = null,
    val courseName: String,
    val courseCode: Int? = null,
    val professorName: String? = null,
    val category: Category,
    val credit: Int = 0,
    val target: String,
    val field: String? = null,
    val courseTime: List<CourseTime> = emptyList(),
){
}
