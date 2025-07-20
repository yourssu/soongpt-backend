package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.department.implement.Department
import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
) {
    fun getByDepartmentGrade(department: Department, grade: Int): Target {
        return targetRepository.getByDepartmentAndGrade(department.id!!, grade)
    }
}
