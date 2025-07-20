package com.yourssu.soongpt.domain.department.implement

interface DepartmentRepository {
    fun saveAll(departments: List<Department>): List<Department>
    fun getByName(name: String): Department
}
