package com.yourssu.soongpt.common.support.fixture

import com.yourssu.soongpt.domain.college.implement.College

enum class CollegeFixture(
) {
    IT대학,
    경영대학;

    fun toDomain(): College {
        return College(
            name = name,
        )
    }
}