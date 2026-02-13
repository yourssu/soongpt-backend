package com.yourssu.soongpt.domain.course.application

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
) {

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
                val summaryItem = ctx.graduationSummary?.majorFoundation
                    ?: return noDataResponse(category, ctx.graduationSummary != null)
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
                    ?: return noDataResponse(category, ctx.graduationSummary != null)
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
                    ?: return noDataResponse(category, ctx.graduationSummary != null)
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
                    ?: return noDataResponse(category, ctx.graduationSummary != null)
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
                if (ctx.graduationSummary == null) {
                    return noGraduationUnavailableResponse(RecommendCategory.RETAKE)
                }
                retakeCourseRecommendService.recommend(
                    lowGradeSubjectCodes = ctx.lowGradeSubjectCodes,
                )
            }

            RecommendCategory.DOUBLE_MAJOR_REQUIRED -> {
                if (ctx.graduationSummary == null) {
                    return noGraduationUnavailableResponse(RecommendCategory.DOUBLE_MAJOR_REQUIRED)
                }
                secondaryMajorCourseRecommendService.recommendDoubleMajorRequired(ctx)
            }

            RecommendCategory.DOUBLE_MAJOR_ELECTIVE -> {
                if (ctx.graduationSummary == null) {
                    return noGraduationUnavailableResponse(RecommendCategory.DOUBLE_MAJOR_ELECTIVE)
                }
                secondaryMajorCourseRecommendService.recommendDoubleMajorElective(ctx)
            }

            RecommendCategory.MINOR -> {
                if (ctx.graduationSummary == null) {
                    return noGraduationUnavailableResponse(RecommendCategory.MINOR)
                }
                secondaryMajorCourseRecommendService.recommendMinor(ctx)
            }

            RecommendCategory.TEACHING -> {
                if (ctx.graduationSummary == null) {
                    return noGraduationUnavailableResponse(RecommendCategory.TEACHING)
                }
                teachingCourseRecommendService.recommend(ctx)
            }
        }
    }

    /** 졸업사정표가 없을 때 재수강/교직 등도 "제공 불가"로 통일 (progress -2) */
    private fun noGraduationUnavailableResponse(category: RecommendCategory): CategoryRecommendResponse {
        val message = when (category) {
            RecommendCategory.RETAKE -> "졸업사정표가 없어 재수강 추천을 제공할 수 없습니다."
            RecommendCategory.TEACHING -> "졸업사정표가 없어 교직이수 추천을 제공할 수 없습니다."
            RecommendCategory.DOUBLE_MAJOR_REQUIRED -> "졸업사정표가 없어 복수전공필수 추천을 제공할 수 없습니다."
            RecommendCategory.DOUBLE_MAJOR_ELECTIVE -> "졸업사정표가 없어 복수전공선택 추천을 제공할 수 없습니다."
            RecommendCategory.MINOR -> "졸업사정표가 없어 부전공 추천을 제공할 수 없습니다."
            else -> "졸업사정표가 없어 해당 추천을 제공할 수 없습니다."
        }
        return CategoryRecommendResponse(
            category = category.name,
            progress = Progress.unavailable(),
            message = message,
            userGrade = null,
            courses = emptyList(),
            lateFields = null,
        )
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

    private fun noDataResponse(
        category: RecommendCategory,
        graduationSummaryExists: Boolean = true,
    ): CategoryRecommendResponse {
        val message = when (category) {
            RecommendCategory.MAJOR_BASIC -> "졸업사정표에 전공기초 항목이 없습니다."
            RecommendCategory.MAJOR_REQUIRED -> "졸업사정표에 전공필수 항목이 없습니다."
            RecommendCategory.MAJOR_ELECTIVE -> "졸업사정표에 전공선택 항목이 없습니다."
            else -> "졸업사정표에 해당 항목이 없습니다."
        }
        val progress = if (graduationSummaryExists) {
            Progress(required = 0, completed = 0, satisfied = true)
        } else {
            Progress.unavailable()
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
}
