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
                it.courseName.contains(course.courseName) && it.professorName == (course.professorName?.replace(" 교수님", "") ?: "")
            }
            pointPairs[course.id!!] = find?.point ?: INIT
        }
        return pointPairs
    }

    private fun findAllBy(courses: Courses): List<Rating> {
        val ratings = mutableListOf<Rating>()
        for (course in courses.values) {
            val find = ratingRepository.findByCourseNameAndProfessorName(
                course.courseName,
                course.professorName?.replace(" 교수님", "") ?: ""
            )
            if (find != null) {
                ratings.add(find)
            }
        }
        return ratings
    }

    fun findBy(courseName: String, professorName: String): Rating {
        return ratingRepository.findByCourseNameAndProfessorName(courseName, professorName)
            ?: throw RatingNotFoundByCourseNameException()
    }
}