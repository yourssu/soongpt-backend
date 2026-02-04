package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.common.validation.ValidSchoolId
import com.yourssu.soongpt.domain.course.application.dto.*
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Course", description = "강의 관련 API")
@RestController
@RequestMapping("/api/courses")
class CourseController(
    private val courseService: CourseService,
) {
    @Operation(
        summary = "강의 필터링 조회 (카테고리별)",
        description = """
            특정 조건에 맞는 강의 목록을 조회합니다.
            
            **파라미터 설명:**
            - **schoolId**: 학번 (필수)
            - **department**: 학과명 (필수)
            - **grade**: 학년 (1~5, 필수)
            - **category**: 이수 구분 (필수). 가능한 값은 다음과 같습니다:
                - `MAJOR_REQUIRED` (전필)
                - `MAJOR_ELECTIVE` (전선)
                - `MAJOR_BASIC` (전기)
                - `GENERAL_REQUIRED` (교필)
                - `GENERAL_ELECTIVE` (교선)
                - `CHAPEL` (채플)
                - `OTHER` (기타)
            - **field**: 세부 영역 (선택). 특정 교양 영역이나 전공 분야 필터링에 사용됩니다.
            - **subDepartment**: 세부 전공 (선택).
        """
    )
    @GetMapping("/by-category")
    fun getCoursesByCategory(@Valid @ModelAttribute request: FilterCoursesRequest): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(summary = "강의 코드로 조회", description = "강의 코드 리스트를 받아 해당하는 강의들의 상세 정보를 조회합니다.")
    @GetMapping
    fun getCoursesByCode(@Valid @ModelAttribute request: GetCoursesByCodeRequest): ResponseEntity<Response<List<CourseDetailResponse>>> {
        val response = courseService.findAllByCode(request.code)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
        summary = "강의 검색",
        description = """
            키워드로 강의를 검색합니다.
            
            **파라미터 설명:**
            - **q**: 검색어 (필수 아님, 기본값 빈 문자열)
            - **page**: 페이지 번호 (0부터 시작, 기본값 0)
            - **size**: 페이지 크기 (1~100, 기본값 20)
            - **sort**: 정렬 방식 (ASC, DESC, 기본값 ASC)
        """
    )
    @GetMapping("/search")
    fun searchCourses(@Valid @ModelAttribute request: SearchCoursesRequest): ResponseEntity<Response<SearchCoursesResponse>> {
        val response = courseService.search(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(summary = "전공 분야/영역 조회", description = "학교 ID에 해당하는 필드(영역/분야) 목록을 조회합니다.")
    @GetMapping("/fields")
    fun getFields(@Valid @ModelAttribute request: GetFieldsRequest): ResponseEntity<Response<Any>> {
        val response = courseService.getFields(request.schoolId)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(hidden = true)
    @Deprecated("Use /api/courses/by-category with field parameter for filtering by field")
    @GetMapping("/fields/schoolId/{schoolId}")
    fun getFieldsBySchoolId(@ValidSchoolId @PathVariable schoolId: Int): ResponseEntity<Response<List<String>>> {
        val response = courseService.getFieldsBySchoolId(schoolId)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(hidden = true)
    @Deprecated("Use /api/courses/by-category instead with category=MAJOR_REQUIRED")
    @GetMapping("/major/required")
    fun getMajorRequiredCourses(@Valid @ModelAttribute request: MajorRequiredRequest): ResponseEntity<Response<List<MajorRequiredResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(hidden = true)
    @Deprecated("Use /api/courses/by-category instead with category=MAJOR_ELECTIVE")
    @GetMapping("/major/elective")
    fun getMajorElectiveCourses(@Valid @ModelAttribute request: MajorElectiveRequest): ResponseEntity<Response<List<MajorElectiveResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(hidden = true)
    @Deprecated("Use /api/courses/by-category instead with category=GENERAL_REQUIRED")
    @GetMapping("/general/required")
    fun getGeneralRequiredCourses(@Valid @ModelAttribute request: GeneralRequiredRequest): ResponseEntity<Response<List<GeneralRequiredResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }
}
