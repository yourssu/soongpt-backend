package com.yourssu.soongpt.domain.coursefield.implement

interface CourseFieldRepository {
    fun findByCourseCode(courseCode: Long): CourseField?
    fun findAll(): List<CourseField>
    fun saveAll(courseFields: List<CourseField>): List<CourseField>
    fun deleteAll()
}
