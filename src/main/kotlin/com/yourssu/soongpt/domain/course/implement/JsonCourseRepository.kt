package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade

interface JsonCourseRepository {
    fun findAllByDepartmentAndGrade(department: Department, departmentGrade: DepartmentGrade, category: Category): List<Course2>
}
