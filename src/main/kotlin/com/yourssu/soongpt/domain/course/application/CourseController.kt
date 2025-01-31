package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.MajorCoreCourseRequest
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
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
    @GetMapping("/major/cores")
    fun getMajorCoreCourses(@Valid @ModelAttribute request: MajorCoreCourseRequest): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findByDepartmentNameInMajorCore(request.department)
        return ResponseEntity.ok().body(Response(result = response))
    }
}