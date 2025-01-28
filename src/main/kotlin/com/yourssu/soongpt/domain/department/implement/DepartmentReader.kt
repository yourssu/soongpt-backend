package com.yourssu.soongpt.domain.department.implement

import com.yourssu.soongpt.domain.department.implement.exception.DepartmentNotFoundByNameException
import org.springframework.stereotype.Component

@Component
class DepartmentReader(
    private val departmentRepository: DepartmentRepository,
) {
    fun getByName(name: String): Department {
        return departmentRepository.findByName(name)
            ?: throw DepartmentNotFoundByNameException()
    }
}