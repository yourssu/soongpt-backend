package com.yourssu.soongpt.domain.rating.implement

class RatingReader (
    private val ratingRepository: RatingRepository
){
    fun findByCode(code: Long): Rating? {
        return ratingRepository.findByCode(code)
    }

    fun findAll(): List<Rating> {
        return ratingRepository.findAll()
    }
}