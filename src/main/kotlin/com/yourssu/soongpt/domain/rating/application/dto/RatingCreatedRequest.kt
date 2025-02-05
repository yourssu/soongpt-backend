package com.yourssu.soongpt.domain.rating.application.dto

import com.yourssu.soongpt.domain.rating.business.RatingCreatedCommand

data class RatingCreatedRequest(
    val course: String,
    val professor: String,
    val courseCode: String,
    val star: Double,
) {
    fun toCommand(): RatingCreatedCommand {
        return RatingCreatedCommand(
            course = course,
            professor = professor,
            courseCode = courseCode,
            star = star,
        )
    }
}