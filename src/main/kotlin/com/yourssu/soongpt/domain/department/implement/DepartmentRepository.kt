package com.yourssu.soongpt.domain.department.implement

interface DepartmentRepository {
    fun save(department: Department): Department
    fun findByName(name: String): Department?
}
