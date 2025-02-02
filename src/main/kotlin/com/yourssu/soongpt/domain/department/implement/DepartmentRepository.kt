package com.yourssu.soongpt.domain.department.implement

interface DepartmentRepository {
    fun save(department: Department): Department
    fun saveAll(departments: List<Department>): List<Department>
    fun findByName(name: String): Department?
}
