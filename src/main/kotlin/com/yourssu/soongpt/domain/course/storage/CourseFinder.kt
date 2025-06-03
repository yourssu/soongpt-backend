package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.Course2
import com.yourssu.soongpt.domain.course.implement.dto.SearchCourseDto
import org.springframework.data.domain.Pageable
import java.util.concurrent.ConcurrentHashMap

object CourseFinder {
    private val cache = ConcurrentHashMap<String, List<Course2>>()

    fun search(
        courses: List<Course2>,
        query: String,
        pageable: Pageable = Pageable.unpaged(),
    ): SearchCourseDto {
        if (query.isBlank()) {
            return SearchCourseDto.empty(pageable)
        }
        if (cache.containsKey(query)) {
            return applyPaging(cache[query]!!, pageable)
        }
        val filteredCourses = filterCourses(courses, query)
        cacheCourses(query, filteredCourses)
        return applyPaging(filteredCourses, pageable)
    }

    private fun filterCourses(
        courses: List<Course2>,
        query: String,
    ): List<Course2> {
        return courses.filter { course ->
            course.courseName.contains(query, ignoreCase = true) ||
                    course.courseCode?.toString()?.contains(query, ignoreCase = true) == true ||
                    course.professorName?.contains(query, ignoreCase = true) == true ||
                    course.category.name.contains(query, ignoreCase = true) ||
                    course.target.contains(query, ignoreCase = true) ||
                    course.field?.contains(query, ignoreCase = true) == true
        }
    }

    private fun cacheCourses(query: String, courses: List<Course2>) {
        cache[query] = courses
    }

    private fun applyPaging(courses: List<Course2>, pageable: Pageable): SearchCourseDto {
        val startIndex = pageable.offset.toInt()
        val endIndex = minOf(startIndex + pageable.pageSize, courses.size)
        if (startIndex >= courses.size) {
            return SearchCourseDto.empty(pageable)
        }
        val pagedCourses = courses.subList(startIndex, endIndex)
        return SearchCourseDto.from(
            courses = pagedCourses,
            totalElements = courses.size.toLong(),
            totalPages = (courses.size + pageable.pageSize - 1) / pageable.pageSize,
            size = pageable.pageSize,
            number = pageable.pageNumber,
        )
    }
}
