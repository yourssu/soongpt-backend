package com.yourssu.soongpt.domain.course.implement

interface CourseRepository {
    fun save(course: Course): Course
    fun findAllByDepartmentGradeId(departmentGradeId: Long, classification: Classification): List<Course>
    fun getAll(ids: List<Long>): List<Course>
    fun findByDepartmentIdAndCourseName(departmentId: Long, courseName: String, classification: Classification): Courses
}
