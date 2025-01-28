package com.yourssu.soongpt.domain.course.implement

interface CourseRepository {
    fun save(course: Course): Course
    fun findAllByDepartmentId(departmentId: Long, classification: Classification): List<Course>
}
