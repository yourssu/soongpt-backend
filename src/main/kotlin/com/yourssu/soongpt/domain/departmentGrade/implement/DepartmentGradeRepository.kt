package com.yourssu.soongpt.domain.departmentGrade.implement

interface DepartmentGradeRepository {
    fun save(departmentGrade: DepartmentGrade): DepartmentGrade
    fun saveAll(departmentGrades: List<DepartmentGrade>): List<DepartmentGrade>
    fun get(id: Long): DepartmentGrade
}