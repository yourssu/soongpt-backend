package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
    private val targetRepository: TargetRepository,

) {
    fun findAllByClass(department: Department, code: Long, grade: Int): List<Course> {
        val targets = targetRepository.findAllByClass(department.id!!, code, grade)
        return courseRepository.findAllById(targets.map { it.courseId })
    }

    fun findAllInCategory(category: Category, courseIds: List<Long>): List<Course> {
        return courseRepository.findAllInCategory(category, courseIds)
    }

    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        return courseRepository.groupByCategory(codes)
    }

    fun searchCourses(query: String, pageable: Pageable): Page<Course> {
        return courseRepository.searchCourses(query, pageable)
    }
}
