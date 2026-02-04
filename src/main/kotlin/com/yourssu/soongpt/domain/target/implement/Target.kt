package com.yourssu.soongpt.domain.target.implement

class Target(
    val id: Long? = null,
    val courseCode: Long,
    val scopeType: ScopeType,
    val collegeId: Long? = null,
    val departmentId: Long? = null,
    val grade1: Boolean = false,
    val grade2: Boolean = false,
    val grade3: Boolean = false,
    val grade4: Boolean = false,
    val grade5: Boolean = false,
    val isDenied: Boolean = false,
    val studentType: StudentType = StudentType.GENERAL,
    val isStrict: Boolean = false,
)
