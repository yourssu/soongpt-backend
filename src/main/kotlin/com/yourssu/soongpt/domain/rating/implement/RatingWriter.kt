package com.yourssu.soongpt.domain.rating.implement

import org.springframework.stereotype.Component

@Component
class RatingWriter (
    private val ratingRepository: RatingRepository
) {
    fun save(rating: Rating): Rating {
        return ratingRepository.save(rating)
    }
}