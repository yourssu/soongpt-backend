package com.yourssu.soongpt.domain.target.implement

interface TargetRepository {
    fun save(target: Target): Target
    fun findAllByCourseId(courseId: Long): List<Target>
    fun findAllByCourseIdToDisplayName(courseId: Long): List<String>
}
