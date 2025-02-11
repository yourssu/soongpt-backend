package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
) {
    fun formatTargetDisplayName(department: Department, departmentGrade: DepartmentGrade): String {
        return "${department.name}${departmentGrade.grade}"
    }

    fun findAllByCourseId(courseId: Long): List<String> {
        return targetRepository.findAllByCourseIdToDisplayName(courseId)
    }
}
