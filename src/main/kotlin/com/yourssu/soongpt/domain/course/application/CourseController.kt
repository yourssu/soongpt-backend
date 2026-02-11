package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.course.application.dto.FilterCoursesRequest
import com.yourssu.soongpt.domain.course.application.dto.GetCoursesByCodeRequest
import com.yourssu.soongpt.domain.course.application.dto.GetCoursesByTrackRequest
import com.yourssu.soongpt.domain.course.application.dto.GetFieldByCodeRequest
import com.yourssu.soongpt.domain.course.application.dto.GetFieldsRequest
import com.yourssu.soongpt.domain.course.application.dto.GetTeachingCoursesRequest
import com.yourssu.soongpt.domain.course.application.dto.RecommendCoursesRequest
import com.yourssu.soongpt.domain.course.application.dto.SearchCoursesRequest
import com.yourssu.soongpt.domain.course.business.CourseService
import com.yourssu.soongpt.domain.course.business.dto.CourseDetailResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseRecommendationsResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import com.yourssu.soongpt.domain.course.business.dto.SearchCoursesResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
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
        private val courseRecommendApplicationService: CourseRecommendApplicationService,
) {
    @Operation(
            summary = "강의 필터링 조회 (카테고리별)",
            description =
                    """
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
                - `TEACHING` (교직)
                - `OTHER` (기타)
            - **field**: 세부 영역 (선택). 특정 교양 영역이나 전공 분야 필터링에 사용됩니다.
            - **subDepartment**: 세부 전공 (선택).
        """
    )
    @GetMapping("/by-category")
    fun getCoursesByCategory(
            @Valid @ModelAttribute request: FilterCoursesRequest
    ): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findAll(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(summary = "강의 코드로 조회", description = "강의 코드 리스트를 받아 해당하는 강의들의 상세 정보를 조회합니다.")
    @GetMapping
    fun getCoursesByCode(
            @Valid @ModelAttribute request: GetCoursesByCodeRequest
    ): ResponseEntity<Response<List<CourseDetailResponse>>> {
        val response = courseService.findAllByCode(request.code)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
            summary = "강의 검색",
            description =
                    """
            키워드로 강의를 검색합니다.

            **파라미터 설명:**
            - **q**: 검색어 (필수 아님, 기본값 빈 문자열)
            - **page**: 페이지 번호 (0부터 시작, 기본값 0)
            - **size**: 페이지 크기 (1~100, 기본값 20)
            - **sort**: 정렬 방식 (ASC, DESC, 기본값 ASC)
        """
    )
    @GetMapping("/search")
    fun searchCourses(
            @Valid @ModelAttribute request: SearchCoursesRequest
    ): ResponseEntity<Response<SearchCoursesResponse>> {
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
            description =
                    """
            과목 코드와 학번을 받아 해당 과목의 교과영역(필드)을 조회합니다.

            **파라미터 설명:**
            - **code**: 과목 코드 리스트 (필수)
            - **schoolId**: 학번 (필수, 예: 24)
        """
    )
    @GetMapping("/field-by-code")
    fun getFieldByCode(
            @Valid @ModelAttribute request: GetFieldByCodeRequest
    ): ResponseEntity<Response<Map<Long, String?>>> {
        val result =
                request.code.associateWith {
                    courseService.getFieldByCourseCode(it, request.schoolId)
                }
        return ResponseEntity.ok().body(Response(result = result))
    }

    @Operation(
            summary = "통합 과목 추천 조회",
            description =
                    """
            SSO 인증된 사용자의 학적정보를 기반으로 모든 이수구분의 과목을 추천합니다.

            **파라미터 설명:**
            - **category**: 추천할 이수구분 (필수). 콤마 구분으로 여러 개 지정 가능. 교양선택은 지원하지 않음.
                - `MAJOR_BASIC` (전공기초)
                - `MAJOR_REQUIRED` (전공필수)
                - `MAJOR_ELECTIVE` (전공선택)
                - `GENERAL_REQUIRED` (교양필수)
                - `RETAKE` (재수강)
                - `DOUBLE_MAJOR_REQUIRED` (복수전공필수)
                - `DOUBLE_MAJOR_ELECTIVE` (복수전공선택)
                - `MINOR` (부전공)
                - `TEACHING` (교직이수) — 교직이수 대상자에게 미이수 교직과목 영역별 추천

            **응답 구조:**
            - 각 이수구분별로 `CategoryRecommendResponse` 반환
            - `progress`: 졸업사정 현황 (required/completed/satisfied). 재수강은 null.
            - `message`: 엣지케이스 안내 메시지 (null이면 정상)
            - `courses`: 추천 과목 flat list (교양은 각 항목에 field 포함, 프론트에서 그룹핑)
            - `lateFields`: 미수강 LATE 분야명 텍스트 (교양필수 전용)

            **인증:**
            - `soongpt_auth` 쿠키(JWT) 필수
            - SyncSessionStore에 캐시된 rusaint 데이터 사용 (동기화 완료된 상태여야 함)
        """,
            security =
                    [
                            io.swagger.v3.oas.annotations.security.SecurityRequirement(
                                    name = "cookieAuth"
                            )]
    )
    @GetMapping("/recommend/all")
    fun recommendCourses(
            @Valid @ModelAttribute request: RecommendCoursesRequest,
            httpRequest: HttpServletRequest,
    ): ResponseEntity<Response<CourseRecommendationsResponse>> {
        val response = courseRecommendApplicationService.recommend(httpRequest, request)
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
            summary = "다전공/부전공 트랙 조회 (트랙별)",
            description =
                    """
            특정 학과의 다전공/부전공 과목을 트랙 유형별로 조회합니다.
            전체 학년(1~5학년)의 과목을 조회합니다.

            **파라미터 설명:**
            - **schoolId**: 학번 (필수)
            - **department**: 학과명 (필수)
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
    fun getCoursesByTrack(
            @Valid @ModelAttribute request: GetCoursesByTrackRequest
    ): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findAllByTrack(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }

    @Operation(
            summary = "교직 과목 조회 (대분류별)",
            description =
                    """
            특정 학과의 교직 과목을 조회합니다. 전체 학년(1~5학년) 대상입니다.

            **파라미터 설명:**
            - **schoolId**: 학번 (필수)
            - **department**: 학과명 (필수)
            - **majorArea**: 교직 대분류 영역 (선택)
                - `전공영역` 또는 `MAJOR`
                - `교직영역` 또는 `TEACHING`
                - `특성화영역` 또는 `SPECIAL`

            **동작:**
            - `majorArea` 미지정: 교직 과목 전체 반환
            - `majorArea` 지정: `course.field` 대분류 prefix 기준 필터링
                - 전공영역 -> `전공영역/...`
                - 교직영역 -> `교직영역/...`
                - 특성화영역 -> `특성화/...`
        """
    )
    @GetMapping("/teaching")
    fun getTeachingCourses(
            @Valid @ModelAttribute request: GetTeachingCoursesRequest
    ): ResponseEntity<Response<List<CourseResponse>>> {
        val response = courseService.findAllTeachingCourses(request.toQuery())
        return ResponseEntity.ok().body(Response(result = response))
    }
}
