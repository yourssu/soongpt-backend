package com.yourssu.soongpt.domain.departmentGrade.implement

interface DepartmentGradeRepository {
    fun save(departmentGrade: DepartmentGrade): DepartmentGrade
    fun saveAll(departmentGrades: List<DepartmentGrade>): List<DepartmentGrade>
    fun get(id: Long): DepartmentGrade
    fun getByDepartmentIdAndGrade(departmentId: Long, grade: Int): DepartmentGrade
    fun getAll(): List<DepartmentGrade>
    fun getByDepartmentId(departmentId: Long): List<DepartmentGrade>
    fun getByGrade(grade: Int): List<DepartmentGrade>
}