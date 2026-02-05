package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CourseRepository {
    fun get(code: Long): Course
    fun findAll(pageable: Pageable): Page<Course>
    fun findAllById(courseIds: List<Long>): List<Course>
    fun findAllByCode(codes: List<Long>): List<Course>
    fun findAllInCategory(category: Category, courseCodes: List<Long>): List<Course>
    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto
    fun searchCourses(query: String, pageable: Pageable): Page<Course>
    fun findAllByClass(code: Long): List<Course>
    fun save(course: Course): Course
}
