package com.yourssu.soongpt.domain.course.implement

import org.springframework.stereotype.Component

@Component
class CoursesManager(
    private val courseReader: CourseReader,
) {
    private val courseCache = mutableMapOf<String, ArrayList<Int>>()

    fun initialCoursesCache() {
        val courses = courseReader.findAll()
        for (course in courses.values) {
            val courseIds = courseCache[course.courseName] ?: ArrayList()
            courseIds.add(courseIds.size)
            courseCache[course.courseName] = courseIds
        }
    }

    fun getCoursesByName(courseName: String): List<Int> {
        return courseCache[courseName] ?: emptyList()
    }
}
