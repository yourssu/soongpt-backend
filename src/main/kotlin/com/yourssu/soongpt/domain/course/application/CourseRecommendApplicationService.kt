package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.domain.course.application.dto.RecommendCategory
import com.yourssu.soongpt.domain.course.application.dto.RecommendCoursesRequest
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.MajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.RetakeCourseRecommendService
import com.yourssu.soongpt.domain.course.business.SecondaryMajorCourseRecommendService
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
    private val secondaryMajorCourseRecommendService: SecondaryMajorCourseRecommendService,
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
                    admissionYear = ctx.admissionYear,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                )
            }

            RecommendCategory.RETAKE -> {
                retakeCourseRecommendService.recommend(
                    lowGradeSubjectCodes = ctx.lowGradeSubjectCodes,
                )
            }

            RecommendCategory.DOUBLE_MAJOR_REQUIRED ->
                secondaryMajorCourseRecommendService.recommendDoubleMajorRequired(ctx)

            RecommendCategory.DOUBLE_MAJOR_ELECTIVE ->
                secondaryMajorCourseRecommendService.recommendDoubleMajorElective(ctx)

            RecommendCategory.MINOR ->
                secondaryMajorCourseRecommendService.recommendMinor(ctx)

            RecommendCategory.TEACHING -> {
                throw IllegalArgumentException("${category.displayName} 추천은 준비 중입니다.")
            }
        }
    }

    private fun progressOnlyResponse(
        category: RecommendCategory,
        progress: Progress,
    ): CategoryRecommendResponse {
        val message = if (progress.satisfied) {
            "${category.displayName} 학점을 이미 모두 이수하셨습니다."
        } else {
            "${category.displayName} 과목 추천 기능은 준비 중입니다."
        }
        return CategoryRecommendResponse(
            category = category.name,
            progress = progress,
            message = message,
            userGrade = null,
            courses = emptyList(),
            lateFields = null,
        )
    }

    private fun noDataResponse(category: RecommendCategory): CategoryRecommendResponse {
        val message = when (category) {
            RecommendCategory.MAJOR_BASIC -> "졸업사정표에 전공기초 항목이 없습니다."
            RecommendCategory.MAJOR_REQUIRED -> "졸업사정표에 전공필수 항목이 없습니다."
            RecommendCategory.MAJOR_ELECTIVE -> "졸업사정표에 전공선택 항목이 없습니다."
            else -> "졸업사정표에 해당 항목이 없습니다."
        }
        return CategoryRecommendResponse(
            category = category.name,
            progress = null,
            message = message,
            userGrade = null,
            courses = emptyList(),
            lateFields = null,
        )
    }
}
