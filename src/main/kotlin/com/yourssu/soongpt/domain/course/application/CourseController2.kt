package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.GeneralRequiredCourseRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorElectiveCourseRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorRequiredCourseRequest
import com.yourssu.soongpt.domain.course.business.CourseService2
import com.yourssu.soongpt.domain.course.business.dto.GeneralRequiredCourseResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorElectiveCourseResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorRequiredCourseResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2/courses")
class CourseController2(
    private val courseService: CourseService2,
) {
    @GetMapping("/major/required")
    fun getMajorRequiredCourses(@Valid @ModelAttribute request: MajorRequiredCourseRequest): ResponseEntity<Response<List<MajorRequiredCourseResponse>>> {
        val response = courseService.findAllByDepartmentNameAndGrade(request.toCommand2())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/major/elective")
    fun getMajorElectiveCourses(@Valid @ModelAttribute request: MajorElectiveCourseRequest): ResponseEntity<Response<List<MajorElectiveCourseResponse>>> {
        val response = courseService.findAllByDepartmentNameAndGrade(request.toCommand2())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/general/elective")
    fun getGeneralElectiveCourses(@Valid @ModelAttribute request: GeneralRequiredCourseRequest): ResponseEntity<Response<List<GeneralRequiredCourseResponse>>?> {
        val response = courseService.findAllByDepartmentNameAndGrade(request.toCommand2())
        return ResponseEntity.ok().body(Response(result = response))
    }
}
