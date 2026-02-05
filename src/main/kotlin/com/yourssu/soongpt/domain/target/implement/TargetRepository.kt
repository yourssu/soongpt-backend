package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun findAllByCode(code: Long): List<Target>
    fun findAllByDepartmentGrade(departmentId: Long, collegeId: Long, grade: Int): List<Long>
    fun findAllByClass(departmentId: Long, code: Long, grade: Int): List<Target>
    fun saveAll(targets: List<Target>): List<Target>
    fun deleteAllByCourseCode(courseCode: Long)
}
