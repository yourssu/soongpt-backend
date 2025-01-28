package com.yourssu.soongpt.domain.target.implement

import org.springframework.stereotype.Component

@Component
class TargetReader(
    private val targetRepository: TargetRepository,
) {
    fun findAllByCourseId(courseId: Long): List<Target> {
        return targetRepository.findAllByCourseId(courseId)
    }
}
