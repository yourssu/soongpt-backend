package com.yourssu.soongpt.domain.courseTime.implement

class CourseTime(
    val id: Long? = null,
    val week: Week,
    val startTime: Int,
    val endTime: Int,
    val classroom: String? = null,
    val courseId: Long,
) {
}