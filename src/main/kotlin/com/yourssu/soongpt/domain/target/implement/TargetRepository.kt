package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun findAllByCode(code: Long): List<Target>
    fun getByDepartmentAndGrade(departmentId: Long, grade: Int): Target
}
