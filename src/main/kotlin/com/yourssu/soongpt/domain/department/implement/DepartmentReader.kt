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

    fun getMatchingDepartments(name: String): List<Department> {
        if (name.equals("경제학과")) {
            val dept = departmentRepository.findByName(name)
            return if (dept != null) listOf(dept) else emptyList()
        }
        val matchedDepartments = departmentRepository.findAll().filter { dept ->
            dept.name.contains(name) || name.contains(dept.name)
        }
        return matchedDepartments
    }

    fun getAll(): List<Department> {
        return departmentRepository.findAll()
    }
}