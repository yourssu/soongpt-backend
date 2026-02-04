package com.yourssu.soongpt.domain.admin.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseTargetResponse
import com.yourssu.soongpt.domain.course.business.dto.SearchCoursesResponse
import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin/courses")
class AdminController(
    private val courseService: CourseService,
) {
    @Operation(
        summary = "전체 과목 조회 (관리자용)",
        description = """
            전체 과목 목록을 페이지네이션하여 조회합니다.

            **파라미터 설명:**
            - **page**: 페이지 번호 (0부터 시작, 기본값 0)
            - **size**: 페이지 크기 (1~100, 기본값 20)
            - **sort**: 정렬 방식 (ASC, DESC, 기본값 ASC)
            - **q**: 검색어 (선택, 빈 문자열이면 전체 조회)
        """
    )
    @GetMapping
    fun getAllCourses(
        @RequestParam(defaultValue = "") q: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "ASC") sort: String,
    ): ResponseEntity<Response<SearchCoursesResponse>> {
        val query = SearchCoursesQuery(
            query = q,
            page = page,
            size = size,
            sort = sort.uppercase(),
        )
        val response = courseService.search(query)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
        summary = "과목 수강 대상 조회 (관리자용)",
        description = """
            특정 과목의 수강 대상 정보를 학과/학년별로 조회합니다.

            **파라미터 설명:**
            - **code**: 과목 코드
        """
    )
    @GetMapping("/{code}/target")
    fun getCourseTarget(
        @PathVariable code: Long
    ): ResponseEntity<Response<CourseTargetResponse>> {
        val response = courseService.getTargetsByCode(code)
        return ResponseEntity.ok().body(Response(result = response))
    }
}