package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.common.infrastructure.slack.SlackWebhookClient
import com.yourssu.soongpt.domain.course.application.dto.RecommendCategory
import com.yourssu.soongpt.domain.course.application.dto.RecommendCoursesRequest
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.MajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.RetakeCourseRecommendService
import com.yourssu.soongpt.domain.course.business.SecondaryMajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.TeachingCourseRecommendService
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.CourseRecommendationsResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.toCategoryResponse
import com.yourssu.soongpt.domain.course.implement.Category
import io.github.oshai.kotlinlogging.KotlinLogging
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
    private val teachingCourseRecommendService: TeachingCourseRecommendService,
    private val slackWebhookClient: SlackWebhookClient,
) {
    private val logger = KotlinLogging.logger {}

    fun recommend(
        request: HttpServletRequest,
        query: RecommendCoursesRequest,
    ): CourseRecommendationsResponse {
        val ctx = contextResolver.resolve(request)
        val categories = query.toCategories()
        val warnings = ctx.warnings.toMutableList()

        if (ctx.graduationSummary == null) {
            warnings.add("NO_GRADUATION_REPORT")
        }

        val results = categories.map { category ->
            dispatch(category, ctx)
        }

        return CourseRecommendationsResponse(
            warnings = warnings,
            categories = results,
        )
    }

    private fun dispatch(
        category: RecommendCategory,
        ctx: RecommendContext,
    ): CategoryRecommendResponse {
        return when (category) {
            RecommendCategory.MAJOR_BASIC -> {
                val progress = progressOrUnavailable(ctx.graduationSummary?.majorFoundation)
                majorCourseRecommendService.recommendMajorBasicOrRequired(
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    category = Category.MAJOR_BASIC,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                ).toCategoryResponse()
            }

            RecommendCategory.MAJOR_REQUIRED -> {
                val progress = progressOrUnavailable(ctx.graduationSummary?.majorRequired)
                majorCourseRecommendService.recommendMajorBasicOrRequired(
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    category = Category.MAJOR_REQUIRED,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                ).toCategoryResponse()
            }

            RecommendCategory.MAJOR_ELECTIVE -> {
                val progress = progressOrUnavailable(ctx.graduationSummary?.majorElective)
                majorCourseRecommendService.recommendMajorElectiveWithGroups(
                    departmentName = ctx.departmentName,
                    userGrade = ctx.userGrade,
                    takenSubjectCodes = ctx.takenSubjectCodes,
                    progress = progress,
                )
            }

            RecommendCategory.GENERAL_REQUIRED -> {
                val progress = progressOrUnavailable(ctx.graduationSummary?.generalRequired)
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

            RecommendCategory.DOUBLE_MAJOR_REQUIRED -> {
                secondaryMajorCourseRecommendService.recommendDoubleMajorRequired(ctx)
            }

            RecommendCategory.DOUBLE_MAJOR_ELECTIVE -> {
                secondaryMajorCourseRecommendService.recommendDoubleMajorElective(ctx)
            }

            RecommendCategory.MINOR -> {
                secondaryMajorCourseRecommendService.recommendMinor(ctx)
            }

            RecommendCategory.TEACHING -> {
                teachingCourseRecommendService.recommend(ctx)
            }
        }
    }

    private fun progressOrUnavailable(summaryItem: com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto?): Progress {
        return if (summaryItem != null && !summaryItem.isEmptyData()) {
            Progress.from(summaryItem)
        } else {
            Progress.unavailable()
        }
    }

}
