package com.yourssu.soongpt.domain.rating.implement

class RatingWriter (
    private val ratingRepository: RatingRepository
) {
    fun save(rating: Rating): Rating {
        return ratingRepository.save(rating)
    }
}