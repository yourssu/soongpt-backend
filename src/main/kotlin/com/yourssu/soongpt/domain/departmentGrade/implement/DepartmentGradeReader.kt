package com.yourssu.soongpt.domain.departmentGrade.implement

import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import org.springframework.stereotype.Component

@Component
class DepartmentGradeReader(
    private val departmentGradeRepository: DepartmentGradeRepository,
    private val departmentReader: DepartmentReader
) {
    fun getByDepartmentIdAndGrade(departmentId: Long, grade: Int): DepartmentGrade {
        return departmentGradeRepository.getByDepartmentIdAndGrade(departmentId, grade)
    }

    fun getAll(): List<DepartmentGrade> {
        return departmentGradeRepository.getAll()
    }

    fun getByDepartmentId(departmentId: Long): List<DepartmentGrade> {
        return departmentGradeRepository.getByDepartmentId(departmentId)
    }

    fun getByGrade(grade: Int): List<DepartmentGrade> {
        return departmentGradeRepository.getByGrade(grade)
    }
}
