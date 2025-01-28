package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.department.implement.Department

enum class DepartmentFixture(
    val departmentName: String,
) {
    COMPUTER(
        departmentName = "컴퓨터학부",
    );

    fun toDomain(collegeId: Long): Department {
        return Department(
            name = departmentName,
            collegeId = collegeId,
        )
    }
}
