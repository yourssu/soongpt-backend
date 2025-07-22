package com.yourssu.soongpt.domain.department.implement

import org.springframework.stereotype.Component

@Component
class DepartmentReader(
    private val departmentRepository: DepartmentRepository,
) {
    fun getByName(name: String): Department {
        return departmentRepository.getByName(name)
    }
}
