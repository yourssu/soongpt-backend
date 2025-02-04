package com.yourssu.soongpt.domain.rating.application.dto

import com.yourssu.soongpt.domain.rating.business.RatingFoundCommand

class RatingFoundRequest(
    val course: String,
    val professor: String,
) {
    fun toCommand(): RatingFoundCommand {
        return RatingFoundCommand(
            course = course,
            professor = professor,
        )
    }

}
