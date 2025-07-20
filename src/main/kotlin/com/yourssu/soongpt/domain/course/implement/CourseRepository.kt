package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.target.implement.Target

interface CourseRepository {
    fun get(code: Long): Course
    fun getAll(codes: List<Long>): List<Course>
    fun findAllByCategoryTarget(category: Category, target: Target): List<Course>
}
