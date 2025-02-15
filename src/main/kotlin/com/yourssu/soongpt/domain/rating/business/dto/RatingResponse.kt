package com.yourssu.soongpt.domain.rating.business.dto

import com.yourssu.soongpt.domain.rating.implement.Rating

data class RatingResponse(
    val course: String,
    val professor: String,
    val star: Double,
    val point: Double,
) {
    companion object {
        fun from(rating: Rating): RatingResponse {
            return RatingResponse(
                course = rating.courseName,
                professor = rating.professorName,
                star = rating.star,
                point = rating.point,
            )
        }
    }
}