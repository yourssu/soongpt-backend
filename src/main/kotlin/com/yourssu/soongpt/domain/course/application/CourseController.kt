package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.*
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.*
import jakarta.validation.Valid
import org.hibernate.validator.constraints.Range
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @Deprecated("Use /api/courses/by-category instead with category=MAJOR_REQUIRED")
    @GetMapping("/major/required")
    fun getMajorRequiredCourses(@Valid @ModelAttribute request: MajorRequiredRequest): ResponseEntity<Response<List<MajorRequiredResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Deprecated("Use /api/courses/by-category instead with category=MAJOR_ELECTIVE")
    @GetMapping("/major/elective")
    fun getMajorElectiveCourses(@Valid @ModelAttribute request: MajorElectiveRequest): ResponseEntity<Response<List<MajorElectiveResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Deprecated("Use /api/courses/by-category instead with category=GENERAL_REQUIRED")
    @GetMapping("/general/required")
    fun getGeneralRequiredCourses(@Valid @ModelAttribute request: GeneralRequiredRequest): ResponseEntity<Response<List<GeneralRequiredResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

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

    @GetMapping("/fields/schoolId/{schoolId}")
    fun getFieldsBySchoolId(@Range(min = 15, max = 27, message = "학번은 15부터 26까지 가능합니다.") @PathVariable schoolId: Int): ResponseEntity<Response<List<String>>> {
        val response = courseService.getFieldsBySchoolId(schoolId)
        return ResponseEntity.ok().body(Response(result = response))
    }
}
