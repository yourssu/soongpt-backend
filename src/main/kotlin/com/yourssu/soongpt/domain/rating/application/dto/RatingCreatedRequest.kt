package com.yourssu.soongpt.domain.rating.application.dto

import com.yourssu.soongpt.domain.rating.business.RatingCreatedCommand

data class RatingCreatedRequest(
    val course: String,
    val professor: String,
    val star: Int,
) {
    fun toCommand(): RatingCreatedCommand {
        return RatingCreatedCommand(
            course = course,
            professor = professor,
            star = star,
        )
    }
}