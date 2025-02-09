package com.yourssu.soongpt.domain.rating.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import org.springframework.stereotype.Component

@Component
class RatingReader(
    private val ratingRepository: RatingRepository,
) {
    fun findAllPointPairs(courses: Courses): Map<Long, Double> {
        val candidates = courses.values.map { Triple(it.id!!, it.courseName, it.professorName?:"") }
        return ratingRepository.findAllByCourseNameAndProfessorName(candidates)
    }
}