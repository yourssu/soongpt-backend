package com.yourssu.soongpt.domain.target.implement

import org.springframework.stereotype.Component

@Component
class TargetWriter(
    private val targetRepository: TargetRepository,
) {
    fun save(target: Target): Target {
        return targetRepository.save(target)
    }
}