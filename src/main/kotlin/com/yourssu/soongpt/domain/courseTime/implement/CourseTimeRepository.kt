package com.yourssu.soongpt.domain.courseTime.implement

interface CourseTimeRepository {
    fun save(courseTime: CourseTime): CourseTime
    fun findAllByCourseId(courseId: Long): List<CourseTime>
}
