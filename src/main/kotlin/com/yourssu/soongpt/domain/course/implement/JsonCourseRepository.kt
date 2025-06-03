package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.SearchCourseDto
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import org.springframework.data.domain.Pageable

interface JsonCourseRepository {
    fun findAllByDepartmentAndGrade(department: Department, departmentGrade: DepartmentGrade, category: Category): List<Course2>
    fun search(query: String, pageable: Pageable): SearchCourseDto
}
