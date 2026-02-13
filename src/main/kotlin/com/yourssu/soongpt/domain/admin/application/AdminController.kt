package com.yourssu.soongpt.domain.admin.application

import com.yourssu.soongpt.common.auth.AdminPasswordValidator
import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.admin.application.exception.UnauthorizedAdminException
import com.yourssu.soongpt.domain.admin.business.AdminCourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseTargetResponse
import com.yourssu.soongpt.domain.course.business.dto.SearchCoursesResponse
import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin/courses")
class AdminController(
    private val adminCourseService: AdminCourseService,
    private val adminPasswordValidator: AdminPasswordValidator,
) {

    private fun requireAdminAuth(password: String?) {
        if (!adminPasswordValidator.validate(password)) {
            throw UnauthorizedAdminException()
        }
    }
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
        val response = adminCourseService.search(query)
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
        val response = adminCourseService.getTargetsByCode(code)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
        summary = "과목 정보 수정 (관리자용)",
        description = """
            과목 정보를 수정합니다. 과목 코드는 수정할 수 없습니다.
            X-Admin-Password 헤더에 관리자 비밀번호가 필요합니다.
        """
    )
    @PutMapping("/{code}")
    fun updateCourse(
        @PathVariable code: Long,
        @RequestBody command: com.yourssu.soongpt.domain.course.business.dto.UpdateCourseCommand,
        @RequestHeader("X-Admin-Password", required = false) password: String?
    ): ResponseEntity<Response<com.yourssu.soongpt.domain.course.business.dto.CourseDetailResponse>> {
        requireAdminAuth(password)
        val response = adminCourseService.updateCourse(code, command)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
        summary = "과목 수강 대상 수정 (관리자용)",
        description = """
            과목의 수강 대상 정책을 전체 수정합니다. 기존 정책은 삭제되고 새로운 정책으로 대체됩니다.
            X-Admin-Password 헤더에 관리자 비밀번호가 필요합니다.
        """
    )
    @PutMapping("/{code}/target")
    fun updateCourseTarget(
        @PathVariable code: Long,
        @RequestBody command: com.yourssu.soongpt.domain.course.business.dto.UpdateTargetsCommand,
        @RequestHeader("X-Admin-Password", required = false) password: String?
    ): ResponseEntity<Response<CourseTargetResponse>> {
        requireAdminAuth(password)
        val response = adminCourseService.updateTargets(code, command)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
        summary = "과목 추가 (관리자용)",
        description = """
            새로운 과목을 추가합니다.
            X-Admin-Password 헤더에 관리자 비밀번호가 필요합니다.
        """
    )
    @PostMapping
    fun createCourse(
        @RequestBody command: com.yourssu.soongpt.domain.course.business.dto.CreateCourseCommand,
        @RequestHeader("X-Admin-Password", required = false) password: String?
    ): ResponseEntity<Response<com.yourssu.soongpt.domain.course.business.dto.CourseDetailResponse>> {
        requireAdminAuth(password)
        val response = adminCourseService.createCourse(command)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
        summary = "과목 삭제 (관리자용)",
        description = """
            과목을 삭제합니다. 관련된 수강 대상 정보도 함께 삭제됩니다.
            X-Admin-Password 헤더에 관리자 비밀번호가 필요합니다.
        """
    )
    @DeleteMapping("/{code}")
    fun deleteCourse(
        @PathVariable code: Long,
        @RequestHeader("X-Admin-Password", required = false) password: String?
    ): ResponseEntity<Response<Unit>> {
        requireAdminAuth(password)
        adminCourseService.deleteCourse(code)
        return ResponseEntity.ok().body(Response(result = Unit))
    }
}