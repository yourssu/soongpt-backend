package com.yourssu.soongpt.domain.course.business.dto

/**
 * 통합 과목 추천 응답 (최상위)
 *
 * result = { categories: CategoryRecommendResponse[] }
 */
data class CourseRecommendationsResponse(
    val warnings: List<String>,
    val categories: List<CategoryRecommendResponse>,
)

/**
 * 이수구분별 추천 결과
 *
 * 모든 이수구분 공통 필드 + 특정 이수구분에서만 사용하는 nullable 필드로 구성.
 *
 * ### 이수구분별 사용 필드 매트릭스
 * | 필드           | 전기/전필 | 전선 | 교필 | 교선 | 재수강 |
 * |---------------|----------|------|------|------|--------|
 * | progress      | O        | O    | O    | O    | X      |
 * | message       | O        | O    | O    | O    | O      |
 * | userGrade     | X        | O    | X    | X    | X      |
 * | courses       | O        | O    | X*   | X*   | O      |
 * | gradeGroups   | X        | O    | X    | X    | X      |
 * | fieldGroups   | X        | X    | O    | O    | X      |
 * | lateFields    | X        | X    | O    | X    | X      |
 *
 * X* = courses는 빈 배열. fieldGroups에서 과목 확인.
 */
data class CategoryRecommendResponse(
    /** 이수구분 (RecommendCategory enum name) */
    val category: String,

    /** 졸업사정 이수 현황 (재수강은 null) */
    val progress: Progress?,

    /** 엣지케이스 안내 메시지 (null이면 정상 — 과목이 존재) */
    val message: String?,

    /** 사용자 학년 (전공선택에서 학년별 구분 시 사용) */
    val userGrade: Int?,

    /** 추천 과목 flat list (교양필수/교양선택은 빈 배열 — fieldGroups 사용) */
    val courses: List<RecommendedCourseResponse>,

    /** 학년별 그룹 (전공선택 전용) */
    val gradeGroups: List<GradeGroupResponse>?,

    /** 분야별 그룹 (교양필수/교양선택 전용, ON_TIME 과목만 포함) */
    val fieldGroups: List<FieldGroupResponse>?,

    /** 미수강 LATE 분야명 (교양필수 전용, 과목 정보 없이 텍스트만) */
    val lateFields: List<String>?,
)

/**
 * 분야별 과목 그룹 (교양필수/교양선택)
 */
data class FieldGroupResponse(
    /** 분야명 (e.g., "SW와AI", "글로벌시민의식") */
    val field: String,

    /** 해당 분야의 추천 과목 목록 */
    val courses: List<RecommendedCourseResponse>,
)

/**
 * MajorCourseRecommendResponse → CategoryRecommendResponse 변환
 */
fun MajorCourseRecommendResponse.toCategoryResponse(
    userGrade: Int? = null,
): CategoryRecommendResponse = CategoryRecommendResponse(
    category = category,
    progress = progress,
    message = message,
    userGrade = userGrade,
    courses = courses,
    gradeGroups = null,
    fieldGroups = null,
    lateFields = null,
)
