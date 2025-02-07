package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
) {
    fun isTarget(courseId: Long, departmentGrade: DepartmentGrade): Boolean {
        return findAllByCourseId(courseId)
            .any { it.departmentGradeId == departmentGrade.id }
    }

    fun formatTargetDisplayName(department: Department, departmentGrade: DepartmentGrade): String {
        return "${department.name}${departmentGrade.grade}"
    }

    private fun findAllByCourseId(courseId: Long): List<Target> {
        return targetRepository.findAllByCourseId(courseId)
    }
}
