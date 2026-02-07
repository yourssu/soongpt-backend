package com.yourssu.soongpt.domain.rating.implement

interface RatingRepository {
    fun save(rating: Rating): Rating
    fun findByCode(code: Long): Rating?
    fun findAll(): List<Rating>
}