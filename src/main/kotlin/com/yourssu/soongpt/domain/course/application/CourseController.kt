package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.*
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseDetailResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import com.yourssu.soongpt.domain.course.business.dto.SearchCoursesResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

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
            - **category**: 이수 구분 (선택). 지정하지 않으면 전체 카테고리 조회. 가능한 값은 다음과 같습니다:
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

    @Operation(
        summary = "과목 코드로 필드 조회",
        description = """
            과목 코드와 학번을 받아 해당 과목의 교과영역(필드)을 조회합니다.

            **파라미터 설명:**
            - **code**: 과목 코드 리스트 (필수)
            - **schoolId**: 학번 (필수, 예: 24)
        """
    )
    @GetMapping("/field-by-code")
    fun getFieldByCode(@Valid @ModelAttribute request: GetFieldByCodeRequest): ResponseEntity<Response<Map<Long, String?>>> {
        val result = request.code.associateWith { courseService.getFieldByCourseCode(it, request.schoolId) }
        return ResponseEntity.ok().body(Response(result = result))
    }

    @Operation(
        summary = "다전공/부전공 트랙 조회 (트랙별)",
        description = """
            특정 학과의 다전공/부전공 과목을 트랙 유형별로 조회합니다.

            **파라미터 설명:**
            - **schoolId**: 학번 (필수)
            - **department**: 학과명 (필수)
            - **grade**: 학년 (1~5, 필수)
            - **trackType**: 트랙 유형 (필수)
                - `DOUBLE_MAJOR` 또는 `복수전공` - 복수전공 과목
                - `MINOR` 또는 `부전공` - 부전공 과목
                - `CROSS_MAJOR` 또는 `타전공인정` - 타전공인정 과목
            - **completionType**: 이수구분 (선택, 없으면 전체 조회)
                - `REQUIRED` 또는 `필수` - 필수 과목만
                - `ELECTIVE` 또는 `선택` - 선택 과목만
                - `RECOGNIZED` 또는 `타전공인정` - 타전공인정 과목만
        """
    )
    @GetMapping("/by-track")
    fun getCoursesByTrack(@Valid @ModelAttribute request: GetCoursesByTrackRequest): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findAllByTrack(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }
}
