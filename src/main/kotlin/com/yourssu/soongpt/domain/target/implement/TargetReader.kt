package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.department.implement.Department
import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
) {
    fun findAllByDepartmentGrade(department: Department, grade: Int): List<Long> {
        return targetRepository.findAllByDepartmentGrade(department.id!!, department.collegeId, grade)
    }
}
