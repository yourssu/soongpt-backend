package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.FieldNullPointException
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
    private val targetRepository: TargetRepository,
    private val fieldListFinder: FieldListFinder,
) {
    fun findAllByClass(department: Department, code: Long, grade: Int): List<Course> {
        val targets = targetRepository.findAllByClass(department.id!!, code, grade)
        return courseRepository.findAllById(targets.map { it.courseId })
    }

    fun findAllBy(category: Category, department: Department, grade: Int): List<Course> {
        val departmentId = department.id ?: return emptyList()
        val targets = targetRepository.findAllByDepartmentGrade(departmentId, grade)
        return courseRepository.findAllInCategory(category, targets.map { it.courseId })
    }

    fun findAllInCategory(category: Category, courseIds: List<Long>, schoolId: Int): List<Course> {
        val courses = courseRepository.findAllInCategory(category, courseIds)
        return courses.map { it -> it.copy(field = FieldFinder.findFieldBySchoolId(it.field?: throw FieldNullPointException(), schoolId)) }
    }

    fun findAllInCategory(category: Category, courseIds: List<Long>, field: String, schoolId: Int): List<Course> {
        val courses = courseRepository.findAllInCategory(category, courseIds)
        return courses.map { it -> it.copy(field = FieldFinder.findFieldBySchoolId(field, schoolId)) }
            .filter { it.field?.contains(field) == true }
    }

    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        return courseRepository.groupByCategory(codes)
    }

    fun searchCourses(query: String, pageable: Pageable): Page<Course> {
        if (query.isBlank()) {
            return courseRepository.findAll(pageable)
        }
        return courseRepository.searchCourses(query, pageable)
    }

    fun findAllByCode(codes: List<Long>): List<Course> {
        return courseRepository.findAllByCode(codes)
    }

    fun getFieldsBySchoolId(schoolId: Int): List<String> {
        return fieldListFinder.getFieldsBySchoolId(schoolId)
    }
}
