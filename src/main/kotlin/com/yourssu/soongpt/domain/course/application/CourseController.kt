package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.GeneralRequiredRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorElectiveRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorRequiredRequest
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.GeneralRequiredResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorElectiveResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorRequiredResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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

//    // TODO: 검색 기능 구현
//    @GetMapping("/search")
//    fun searchCourses(@Valid @ModelAttribute request: SearchCoursesRequest): ResponseEntity<Response<SearchCoursesResponse>> {
//        val response = courseService.search(request.toCommand())
//        return ResponseEntity.ok().body(Response(result = response))
//    }
}
