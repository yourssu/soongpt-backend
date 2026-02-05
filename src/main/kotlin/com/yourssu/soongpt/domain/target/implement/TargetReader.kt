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

    fun findAllByCode(code: Long): List<Target> {
        return targetRepository.findAllByCode(code)
    }

    fun saveAll(targets: List<Target>): List<Target> {
        return targetRepository.saveAll(targets)
    }

    fun deleteAllByCourseCode(courseCode: Long) {
        targetRepository.deleteAllByCourseCode(courseCode)
    }
}
