package com.yourssu.soongpt.domain.rating.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.rating.implement.Rating.Companion.INIT
import com.yourssu.soongpt.domain.rating.implement.exception.RatingNotFoundByCourseNameException
import org.springframework.stereotype.Component

@Component
class RatingReader(
    private val ratingRepository: RatingRepository,
) {
    fun findAllPointPairs(courses: Courses): Map<Long, Double> {
        val ratings = findAllBy(courses)
        val pointPairs = mutableMapOf<Long, Double>()
        for (course in courses.values) {
            val find = ratings.find {
                it.courseName == course.courseName && it.professorName == course.professorName
            }
            pointPairs[course.id!!] = find?.point ?: INIT
        }
        return pointPairs
    }

    private fun findAllBy(courses: Courses): List<Rating> {
        val ratings = mutableListOf<Rating>()
        for (course in courses.values) {
            val rating = ratingRepository.findByCourseNameAndProfessorName(
                course.courseName,
                course.professorName?: ""
            )
            if (rating != null) {
                ratings.add(rating)
            }
        }
        return ratings
    }

    fun findBy(courseName: String, professorName: String): Rating {
        return ratingRepository.findByCourseNameAndProfessorName(courseName, professorName)
            ?: throw RatingNotFoundByCourseNameException()
    }
}