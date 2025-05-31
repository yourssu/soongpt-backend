package com.yourssu.soongpt.domain.course.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.GeneralRequiredCourseRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorElectiveCourseRequest
import com.yourssu.soongpt.domain.course.application.dto.MajorRequiredCourseRequest
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

private val logger = KotlinLogging.logger {}
private val mapper = ObjectMapper()

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @GetMapping("/major/required")
    fun getMajorRequiredCourses(@Valid @ModelAttribute request: MajorRequiredCourseRequest): ResponseEntity<Response<List<CourseResponse>>> {
        logger.info { "GET /api/courses/major/required  request: ${mapper.writeValueAsString(request)}" }
        val response = courseService.findByDepartmentNameInMajorRequired(request.toCommand())
        logger.info { "GET /api/courses/major/required response: ${mapper.writeValueAsString(response)}" }
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/major/elective")
    fun getMajorElectiveCourses(@Valid @ModelAttribute request: MajorElectiveCourseRequest): ResponseEntity<Response<List<CourseResponse>>> {
        logger.info { "GET /api/courses/major/elective  request: ${mapper.writeValueAsString(request)}" }
        val response = courseService.findByDepartmentNameInMajorElective(request.toCommand())
        logger.info { "GET /api/courses/major/elective response: ${mapper.writeValueAsString(response)}" }
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping("/general/required")
    fun getGeneralRequiredCourses(@Valid @ModelAttribute request: GeneralRequiredCourseRequest): ResponseEntity<Response<List<CourseResponse>>> {
        logger.info { "GET /api/courses/general/required  request: ${mapper.writeValueAsString(request)}" }
        val response = courseService.findByDepartmentNameInGeneralRequired(request.toCommand())
        logger.info { "GET /api/courses/general/required response: ${mapper.writeValueAsString(response)}" }
        return ResponseEntity.ok().body(Response(result = response))
    }

    @GetMapping
    fun getCourse(@RequestParam courseId: List<Long>): ResponseEntity<Response<List<CourseResponse>>> {
        logger.info { "GET /api/courses request: ${mapper.writeValueAsString(courseId)}" }
        val responses = courseId.map { courseService.findById(it) }
        logger.info { "GET /api/courses response: ${mapper.writeValueAsString(responses)}" }
        return ResponseEntity.ok().body(Response(result = responses))
    }
}
