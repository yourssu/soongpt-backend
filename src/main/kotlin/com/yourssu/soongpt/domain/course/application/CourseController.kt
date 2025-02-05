package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.CreateCourseRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorRequiredCourseRequest
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping("/major/required")
    fun getMajorRequiredCourses(@Valid @ModelAttribute request: MajorRequiredCourseRequest): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findByDepartmentNameInMajorRequired(request.department)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @PostMapping
    fun createCourses(@RequestBody courses: List<CreateCourseRequest>): ResponseEntity<String> {
        courseService.createCourses(courses)
        return ResponseEntity.ok("과목 정보 저장 완료")
    }
}