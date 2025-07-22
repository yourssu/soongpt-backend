package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun findAllByCourseId(courseId: Long): List<Target>
    fun findAllByDepartmentGrade(departmentId: Long, grade: Int): List<Target>
}
