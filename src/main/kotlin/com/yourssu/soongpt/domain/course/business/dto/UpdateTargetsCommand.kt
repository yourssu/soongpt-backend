package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType

data class UpdateTargetsCommand(val targets: List<TargetCommand>)

data class TargetCommand(
        val id: Long? = null,
        val scopeType: ScopeType,
        val scopeId: Long?,
        val scopeName: String?,
        val grade1: Boolean,
        val grade2: Boolean,
        val grade3: Boolean,
        val grade4: Boolean,
        val grade5: Boolean,
        val studentType: StudentType,
        val isStrict: Boolean,
        val isDenied: Boolean
)
