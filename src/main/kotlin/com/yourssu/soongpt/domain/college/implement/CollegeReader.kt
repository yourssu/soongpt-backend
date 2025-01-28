package com.yourssu.soongpt.domain.college.implement

import org.springframework.stereotype.Component

@Component
class CollegeReader(
    private val collegeRepository: CollegeRepository,
) {
    fun get(id: Long): College {
        return collegeRepository.get(id)
    }
}