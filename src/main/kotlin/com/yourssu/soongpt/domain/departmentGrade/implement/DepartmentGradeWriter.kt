package com.yourssu.soongpt.domain.departmentGrade.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import org.springframework.stereotype.Component

@Component
class DepartmentGradeWriter(
    private val departmentRepository: DepartmentRepository,
) {
    fun save(department: Department): Department {
        return departmentRepository.save(department)
    }
}