package com.yourssu.soongpt.domain.course.business

data class ParsedTarget(
    val grade: Int,
    val includedDepartments: Set<String>,
    val excludedDepartments: Set<String>,
)
