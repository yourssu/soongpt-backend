package com.yourssu.soongpt.domain.rating.business

import com.yourssu.soongpt.domain.rating.implement.Rating

class RatingCreatedCommand(
    val course: String,
    val professor: String,
    val star: Int,
) {
    fun toRating(): Rating {
        return Rating(
            courseName = course,
            professorName = professor,
            star = star,
        )
    }
}
