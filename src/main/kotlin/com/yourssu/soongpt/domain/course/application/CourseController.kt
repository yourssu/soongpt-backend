package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.*
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.*
import jakarta.validation.Valid
import org.jetbrains.annotations.NotNull
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping("/major/required")
    fun getMajorRequiredCourses(@Valid @ModelAttribute request: MajorRequiredRequest): ResponseEntity<Response<List<MajorRequiredResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/major/elective")
    fun getMajorElectiveCourses(@Valid @ModelAttribute request: MajorElectiveRequest): ResponseEntity<Response<List<MajorElectiveResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/general/required")
    fun getGeneralRequiredCourses(@Valid @ModelAttribute request: GeneralRequiredRequest): ResponseEntity<Response<List<GeneralRequiredResponse>>> {
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

    @GetMapping("/fields/schoolId/{schoolId}")
    fun getFieldsBySchoolId(@NotNull @PathVariable schoolId: Int): ResponseEntity<Response<List<String>>> {
        val response = courseService.getFieldsBySchoolId(schoolId)
        return ResponseEntity.ok().body(Response(result = response))
    }
}
