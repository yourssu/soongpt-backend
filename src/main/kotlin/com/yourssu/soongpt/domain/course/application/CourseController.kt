package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.application.dto.*
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.*
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping("/by-category")
    fun getCoursesByCategory(@Valid @ModelAttribute request: FilterCoursesRequest): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping
    fun getCoursesByCode(@Valid @ModelAttribute request: GetCoursesByCodeRequest): ResponseEntity<Response<List<CourseDetailResponse>>> {
        val response = courseService.findAllByCode(request.code)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/search")
    fun searchCourses(@Valid @ModelAttribute request: SearchCoursesRequest): ResponseEntity<Response<SearchCoursesResponse>> {
        val response = courseService.search(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/fields")
    fun getFields(@Valid @ModelAttribute request: GetFieldsRequest): ResponseEntity<Response<Any>> {
        val response = courseService.getFields(request.schoolId)
        return ResponseEntity.ok().body(Response(result = response))
    }


}
