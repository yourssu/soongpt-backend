package com.yourssu.soongpt.domain.departmentGrade.implement

import org.springframework.stereotype.Component

@Component
class DepartmentGradeWriter(
    private val departmentRepository: DepartmentGradeRepository,
) {
    fun save(departmentGrade: DepartmentGrade): DepartmentGrade {
        return departmentRepository.save(departmentGrade)
    }
    fun saveAll(departmentGrades: List<DepartmentGrade>): List<DepartmentGrade> {
        return departmentRepository.saveAll(departmentGrades)
    }
}