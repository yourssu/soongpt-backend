package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun findAllByCode(code: Long): List<Target>
    fun findAllByDepartmentGrade(departmentId: Long, collegeId: Long, grade: Int): List<Long>
    fun findAllByClass(departmentId: Long, code: Long, grade: Int): List<Target>

    /** 1학년부터 maxGrade까지 해당하는 과목 코드 조회 (전기/전필용) */
    fun findAllByDepartmentGradeRange(departmentId: Long, collegeId: Long, maxGrade: Int): List<Long>
}
