package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade

interface CourseRepository {
    fun findAllByDepartmentId(departmentId: Long, classification: Classification): List<Pair<Course, List<DepartmentGrade>>>
    fun findAllByDepartmentGradeId(departmentGradeId: Long, classification: Classification): List<Course>
    fun getAll(ids: List<Long>): List<Course>
    fun findByDepartmentIdAndCourseName(departmentId: Long, courseName: String, classification: Classification): Courses
    fun findByDepartmentGradeIdAndCourseName(departmentGradeId: Long, courseName: String, classification: Classification): Courses
    fun findChapelsByDepartmentGradeId(departmentGradeId: Long): List<Course>
    fun get(courseId: Long): Course
    fun findAll(): Courses
}
