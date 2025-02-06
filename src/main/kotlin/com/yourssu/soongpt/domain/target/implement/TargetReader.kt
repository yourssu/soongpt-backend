package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeRepository
import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
    private val departmentGradeRepository: DepartmentGradeRepository,
) {
    fun findAllBy(courseId: Long, department: Department): List<String> {
        val targets = findAllByCourseId(courseId)
            .map { departmentGradeRepository.get(it.departmentGradeId) }
            .filter { it.departmentId == department.id }
        return targets.map { formatTargetDisplayName(department, it) }
    }

    fun formatTargetDisplayName(department: Department, departmentGrade: DepartmentGrade): String {
        return "${department.name}${departmentGrade.grade}"
    }

    private fun findAllByCourseId(courseId: Long): List<Target> {
        return targetRepository.findAllByCourseId(courseId)
    }
}
