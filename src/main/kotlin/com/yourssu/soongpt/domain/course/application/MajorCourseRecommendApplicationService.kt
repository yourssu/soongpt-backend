package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.domain.course.application.dto.RecommendMajorCoursesRequest
import com.yourssu.soongpt.domain.course.business.MajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.RetakeCourseRecommendService
import com.yourssu.soongpt.domain.course.business.dto.MajorCourseRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.implement.Category
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

/**
 * 전공 과목 추천 애플리케이션 서비스
 * - SSO 인증 및 동기화 상태 확인
 * - rusaint 데이터 조회 및 변환
 * - 비즈니스 서비스 호출
 */
@Service
class MajorCourseRecommendApplicationService(
    private val contextResolver: RecommendContextResolver,
    private val majorCourseRecommendService: MajorCourseRecommendService,
    private val retakeCourseRecommendService: RetakeCourseRecommendService,
) {

    /**
     * 전공 과목 추천 조회
     */
    fun recommend(
        request: HttpServletRequest,
        query: RecommendMajorCoursesRequest,
    ): MajorCourseRecommendResponseDto {
        val ctx = contextResolver.resolve(request)

        // 요청된 카테고리 목록 (없으면 전기/전필/전선 모두)
        val categories = query.toCategories()

        // 각 카테고리별 추천 결과 생성
        val categoryGroups = categories.map { category ->
            val progress = when (category) {
                Category.MAJOR_BASIC -> Progress.from(ctx.graduationSummary.majorFoundation)
                Category.MAJOR_REQUIRED -> Progress.from(ctx.graduationSummary.majorRequired)
                Category.MAJOR_ELECTIVE -> Progress.from(ctx.graduationSummary.majorElective)
                else -> throw IllegalArgumentException("Invalid category: $category")
            }

            when (category) {
                Category.MAJOR_BASIC, Category.MAJOR_REQUIRED -> {
                    majorCourseRecommendService.recommendMajorBasicOrRequired(
                        departmentName = ctx.departmentName,
                        userGrade = ctx.userGrade,
                        category = category,
                        takenSubjectCodes = ctx.takenSubjectCodes,
                        progress = progress,
                    )
                }
                Category.MAJOR_ELECTIVE -> {
                    majorCourseRecommendService.recommendMajorElective(
                        departmentName = ctx.departmentName,
                        userGrade = ctx.userGrade,
                        takenSubjectCodes = ctx.takenSubjectCodes,
                        progress = progress,
                    )
                }
                else -> throw IllegalArgumentException("Invalid category: $category")
            }
        }.toMutableList()

        // RETAKE 카테고리 처리 (기본값에는 미포함, 명시적 요청 시에만)
        if (query.includesRetake()) {
            val retakeGroup = retakeCourseRecommendService.recommend(
                lowGradeSubjectCodes = ctx.lowGradeSubjectCodes,
            )
            categoryGroups += retakeGroup
        }

        return MajorCourseRecommendResponseDto(
            categories = categoryGroups,
        )
    }
}

/**
 * 전공 과목 추천 응답 DTO
 * 명세서: result = { categories: CategoryGroup[] }
 */
data class MajorCourseRecommendResponseDto(
    val categories: List<MajorCourseRecommendResponse>,
)
