package com.yourssu.soongpt.domain.departmentGrade.implement

interface DepartmentGradeRepository {
    fun save(departmentGrade: DepartmentGrade): DepartmentGrade
    fun get(id: Long): DepartmentGrade
}