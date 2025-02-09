package com.yourssu.soongpt.domain.rating.implement

interface RatingRepository {
    fun save(rating: Rating): Rating
    fun findByCourseNameAndProfessorName(courseName: String, professorName: String): Rating?
    fun findAllByCourseNameAndProfessorName(pairs: List<Triple<Long, String, String>>): Map<Long, Double>
}