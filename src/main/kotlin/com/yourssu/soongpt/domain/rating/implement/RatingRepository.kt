package com.yourssu.soongpt.domain.rating.implement

interface RatingRepository {
    fun save(rating: Rating): Rating
    fun findByCourseNameAndProfessorName(courseName: String, professorName: String): Rating?
}