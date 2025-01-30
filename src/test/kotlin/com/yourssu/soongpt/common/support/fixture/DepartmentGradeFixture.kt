package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade

enum class DepartmentGradeFixture(
    val grade: Int,
) {
    FIRST(
        grade = 1,
    ),
    SECOND(
        grade = 2,
    ),
    THIRD(
        grade = 3,
    ),
    FOURTH(
        grade = 4,
    ),
    FIFTH(
        grade = 5,
    );

    fun toDomain(departmentId: Long): DepartmentGrade {
        return DepartmentGrade(
            grade = grade,
            departmentId = departmentId,
        )
    }
}