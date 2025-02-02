package com.yourssu.soongpt.domain.department.implement

import org.springframework.stereotype.Component

@Component
class DepartmentWriter(
    private val departmentRepository: DepartmentRepository,
) {
    fun save(department: Department): Department {
        return departmentRepository.save(department)
    }
    fun saveAll(departments: List<Department>): List<Department> {
        return departmentRepository.saveAll(departments)
    }
}
