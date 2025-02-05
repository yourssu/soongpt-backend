package com.yourssu.soongpt.domain.rating.implement

import com.yourssu.soongpt.domain.rating.implement.exception.RatingNotFoundByCourseNameException
import org.springframework.stereotype.Component

@Component
class RatingReader(
    private val ratingRepository: RatingRepository,
) {
    fun findBy(courseName: String, professorName: String): Rating {
        return ratingRepository.findByCourseNameAndProfessorName(courseName, professorName)
            ?: throw RatingNotFoundByCourseNameException()
    }
}