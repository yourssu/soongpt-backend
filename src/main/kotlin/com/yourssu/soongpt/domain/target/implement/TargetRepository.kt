package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun save(target: Target): Target
    fun get(id: Long): Target
}