package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.target.implement.Target

interface CourseRepository {
    fun get(code: Long): Course
    fun getAll(code: List<Long>): List<Course>
    fun findTargetsByCourseId(courseId: Long): List<Target>
}
