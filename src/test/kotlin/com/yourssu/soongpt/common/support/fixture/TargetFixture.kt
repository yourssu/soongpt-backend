package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.target.implement.Target

enum class TargetFixture {
    TARGET1;

    fun toDomain(departmentGradeId: Long, courseId: Long): Target {
        return Target(
            departmentId = departmentGradeId,
            courseId = courseId,
        )
    }
}
