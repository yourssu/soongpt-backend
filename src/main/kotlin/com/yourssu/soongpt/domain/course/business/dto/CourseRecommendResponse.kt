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
 * 그룹핑(학년별, 분야별)은 프론트엔드에서 courses의 field/targetGrades를 기준으로 수행.
 *
 * ### 이수구분별 사용 필드 매트릭스
 * | 필드           | 전기/전필 | 전선 | 교필 | 교선 | 재수강 |
 * |---------------|----------|------|------|------|--------|
 * | progress      | O        | O    | O    | O    | O(-1)  |
 * | message       | O        | O    | O    | O    | O      |
 * | userGrade     | X        | O    | X    | X    | X      |
 * | courses       | O        | O    | O    | O    | O      |
 * | lateFields    | X        | X    | O    | X    | X      |
 *
 * 교필/교선: courses 각 항목에 field 포함. 프론트에서 field로 그룹핑.
 */
data class CategoryRecommendResponse(
    /** 이수구분 (RecommendCategory enum name) */
    val category: String,

    /** 졸업사정 이수 현황. 항상 non-null. 센티널: required=-1 → 재수강/교직(bar 미표시), -2 → 졸업사정표 없음 */
    val progress: Progress,

    /** 엣지케이스 안내 메시지 (null이면 정상 — 과목이 존재) */
    val message: String?,

    /** 사용자 학년 (전공선택에서 학년별 구분 시 사용) */
    val userGrade: Int?,

    /** 추천 과목 flat list (교필/교선은 각 항목에 field 포함) */
    val courses: List<RecommendedCourseResponse>,

    /** 미수강 LATE 분야명 (교양필수 전용, 과목 정보 없이 텍스트만) */
    val lateFields: List<String>?,
)

/**
 * CategoryRecommendResult → CategoryRecommendResponse 변환
 */
fun CategoryRecommendResult.toCategoryResponse(
    userGrade: Int? = null,
): CategoryRecommendResponse = CategoryRecommendResponse(
    category = category,
    progress = progress,
    message = message,
    userGrade = userGrade,
    courses = courses,
    lateFields = null,
)
