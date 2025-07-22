package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.storage.exception.DepartmentNotFoundException
import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
) {
    fun findAllByDepartmentGrade(department: Department, grade: Int): List<Target> {
        val departmentId = department.id?: throw DepartmentNotFoundException()
        return targetRepository.findAllByDepartmentGrade(departmentId, grade)
    }
}
