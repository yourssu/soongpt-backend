package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.domain.course.application.dto.RecommendCategory
import com.yourssu.soongpt.domain.course.application.dto.RecommendCoursesRequest
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.MajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.RetakeCourseRecommendService
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseRecommendationsResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.toCategoryResponse
import com.yourssu.soongpt.domain.course.implement.Category
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service

/**
 * 통합 과목 추천 애플리케이션 서비스
 * - GET /api/courses/recommend/all 엔드포인트용
 * - 전기/전필/전선/교필/재수강 등 모든 이수구분 처리
 */
@Service
class CourseRecommendApplicationService(
    private val contextResolver: RecommendContextResolver,
    private val majorCourseRecommendService: MajorCourseRecommendService,
    private val generalCourseRecommendService: GeneralCourseRecommendService,
    private val retakeCourseRecommendService: RetakeCourseRecommendService,
) {

    fun recommend(
        request: HttpServletRequest,
        query: RecommendCoursesRequest,
    ): CourseRecommendationsResponse {
        val ctx = contextResolver.resolve(request)
        val categories = query.toCategories()

        val results = categories.map { category ->
            dispatch(category, ctx)
        }

        return CourseRecommendationsResponse(
            warnings = ctx.warnings,
            categories = results,
        )
    }

    private fun dispatch(
        category: RecommendCategory,
        ctx: RecommendContext,
    ): CategoryRecommendResponse {
        return when (category) {
            RecommendCategory.MAJOR_BASIC -> {
                val summaryItem = ctx.graduationSummary?.majorFoundation
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                majorCourseRecommendService.recommendMajorBasicOrRequired(
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    category = Category.MAJOR_BASIC,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                ).toCategoryResponse()
            }

            RecommendCategory.MAJOR_REQUIRED -> {
                val summaryItem = ctx.graduationSummary?.majorRequired
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                majorCourseRecommendService.recommendMajorBasicOrRequired(
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    category = Category.MAJOR_REQUIRED,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                ).toCategoryResponse()
            }

            RecommendCategory.MAJOR_ELECTIVE -> {
                val summaryItem = ctx.graduationSummary?.majorElective
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                majorCourseRecommendService.recommendMajorElectiveWithGroups(
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                )
            }

            RecommendCategory.GENERAL_REQUIRED -> {
                val summaryItem = ctx.graduationSummary?.generalRequired
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                generalCourseRecommendService.recommend(
                    category = Category.GENERAL_REQUIRED,
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    schoolId = ctx.schoolId,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                )
            }

            RecommendCategory.RETAKE -> {
                retakeCourseRecommendService.recommend(
                    lowGradeSubjectCodes = ctx.lowGradeSubjectCodes,
                ).toCategoryResponse()
            }

            RecommendCategory.GENERAL_ELECTIVE -> {
                throw IllegalArgumentException("교양선택은 별도 API로 제공 예정입니다.")
            }

            RecommendCategory.DOUBLE_MAJOR_REQUIRED -> {
                val summaryItem = ctx.graduationSummary?.doubleMajorRequired
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                noDataResponse(category).copy(progress = progress)
            }

            RecommendCategory.DOUBLE_MAJOR_ELECTIVE -> {
                val summaryItem = ctx.graduationSummary?.doubleMajorElective
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                noDataResponse(category).copy(progress = progress)
            }

            RecommendCategory.MINOR -> {
                val summaryItem = ctx.graduationSummary?.minor
                    ?: return noDataResponse(category)
                val progress = Progress.from(summaryItem)
                noDataResponse(category).copy(progress = progress)
            }

            RecommendCategory.TEACHING -> {
                throw IllegalArgumentException("${category.displayName} 추천은 준비 중입니다.")
            }
        }
    }

    private fun noDataResponse(category: RecommendCategory) = CategoryRecommendResponse(
        category = category.name,
        progress = null,
        message = null,
        userGrade = null,
        courses = emptyList(),
        gradeGroups = null,
        fieldGroups = null,
        lateFields = null,
    )
}
