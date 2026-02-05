package com.yourssu.soongpt.domain.rating.implement

import org.springframework.stereotype.Component

@Component
class RatingReader (
    private val ratingRepository: RatingRepository
){
    fun findByCode(code: Long): Rating? {
        return ratingRepository.findByCode(code)
    }

    fun findAll(): List<Rating> {
        return ratingRepository.findAll()
    }

    fun findAllByCourseCodes(courseCodes: List<Long>): List<Rating> {
        return ratingRepository.findAllByCourseCodes(courseCodes)
    }
}
