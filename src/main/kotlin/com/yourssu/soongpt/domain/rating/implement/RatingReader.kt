package com.yourssu.soongpt.domain.rating.implement

import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.rating.implement.exception.RatingNotFoundByCourseNameException
import org.springframework.stereotype.Component

@Component
class RatingReader(
    private val ratingRepository: RatingRepository,
) {
    fun findAllBy(courses: Courses): List<Rating> {
        return courses.unpackNameAndProfessor().mapNotNull { (courseName, professorName) ->
            ratingRepository.findByCourseNameAndProfessorName(courseName, professorName)
        }
    }


    fun findBy(courseName: String, professorName: String): Rating {
        return ratingRepository.findByCourseNameAndProfessorName(courseName, professorName)
            ?: throw RatingNotFoundByCourseNameException()
    }
}