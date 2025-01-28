package com.yourssu.soongpt.domain.college.implement

import org.springframework.stereotype.Component

@Component
class CollegeWriter(
    private val collegeRepository: CollegeRepository,
) {
    fun save(college: College): College {
        return collegeRepository.save(college)
    }
}