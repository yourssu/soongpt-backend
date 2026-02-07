package com.yourssu.soongpt.domain.department.implement

import org.springframework.stereotype.Component

@Component
class DepartmentWriter(
    private val departmentRepository: DepartmentRepository,
) {
    fun saveAll(departments: List<Department>): List<Department> {
        return departmentRepository.saveAll(departments)
    }
}
