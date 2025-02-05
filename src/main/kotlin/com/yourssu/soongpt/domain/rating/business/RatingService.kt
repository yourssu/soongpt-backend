package com.yourssu.soongpt.domain.rating.business

import com.yourssu.soongpt.domain.rating.business.dto.RatingResponse
import com.yourssu.soongpt.domain.rating.implement.RatingReader
import com.yourssu.soongpt.domain.rating.implement.RatingWriter
import org.springframework.stereotype.Service

@Service
class RatingService(
    private val ratingWriter: RatingWriter,
    private val ratingReader: RatingReader,
) {
    fun save(command: RatingCreatedCommand): RatingResponse {
        val rating = ratingWriter.save(command.toRating())
        return RatingResponse.from(rating)
    }

    fun findBy(command: RatingFoundCommand): RatingResponse {
        val rating = ratingReader.findBy(courseName = command.course, professorName = command.professor)
        return RatingResponse.from(rating)
    }

}