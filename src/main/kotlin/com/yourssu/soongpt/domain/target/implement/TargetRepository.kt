package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun findAllByCode(code: Long): List<Target>
    fun findAllByDepartmentGrade(departmentId: Long, grade: Int): List<Target>
}
