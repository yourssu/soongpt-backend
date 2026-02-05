package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.course.implement.Category
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

    /**
     * 카테고리에 따라 적절한 학년 범위로 과목 코드 조회
     * - 전기/전필: 1~현재학년 (상한 제한)
     * - 전선: 전체 학년 (제한 없음)
     */
    fun findCourseCodesByCategory(
        department: Department,
        userGrade: Int,
        category: Category
    ): List<Long> {
        return when (category) {
            Category.MAJOR_BASIC, Category.MAJOR_REQUIRED ->
                targetRepository.findAllByDepartmentGradeRange(
                    department.id!!, department.collegeId, userGrade
                )
            Category.MAJOR_ELECTIVE ->
                targetRepository.findAllByDepartmentGradeRange(
                    department.id!!, department.collegeId, 5
                )
            else -> emptyList()
        }
    }

    fun saveAll(targets: List<Target>): List<Target> {
        return targetRepository.saveAll(targets)
    }

    fun deleteAllByCourseCode(courseCode: Long) {
        targetRepository.deleteAllByCourseCode(courseCode)
    }
}
