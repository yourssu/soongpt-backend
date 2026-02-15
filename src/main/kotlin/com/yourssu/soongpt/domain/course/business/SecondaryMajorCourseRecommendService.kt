package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.application.RecommendContext
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toTakenBaseCodeSet
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import org.springframework.stereotype.Service

/**
 * 복수전공·부전공 과목 추천 서비스
 * - 복필/복선: ctx.flags.doubleMajorDepartment 기준
 * - 부전공: ctx.flags.minorDepartment 기준, 부필+부선을 field로 구분해 flat courses로 전달
 * - 그룹핑은 프론트에서 수행. 복선은 targetGrades+userGrade, 부전공은 field(부필/부선)만 전달.
 */
@Service
class SecondaryMajorCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
) {

    /**
     * 복수전공필수 추천
     * - 학년 구분 없이 flat courses, field = "복필"
     */
    fun recommendDoubleMajorRequired(ctx: RecommendContext): CategoryRecommendResponse {
        val departmentName = ctx.flags.doubleMajorDepartment
            ?: return notRegisteredResponse("DOUBLE_MAJOR_REQUIRED", "복수전공을 등록하지 않았습니다.")
        val progress = progressOrUnavailable(ctx.graduationSummary?.doubleMajorRequired)
        if (progress.satisfied) {
            return satisfiedResponse("DOUBLE_MAJOR_REQUIRED", progress, "복수전공필수 학점을 이미 모두 이수하셨습니다.")
        }
        val department = departmentReader.getByName(departmentName)
        val coursesWithTarget = courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = SecondaryMajorTrackType.DOUBLE_MAJOR,
            completionType = SecondaryMajorCompletionType.REQUIRED,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            userGrade = ctx.userGrade,
            maxGrade = ctx.userGrade,
        )
        val takenBaseCodes = toTakenBaseCodeSet(ctx.takenSubjectCodes)
        val untaken = coursesWithTarget.filter { it.course.baseCode() !in takenBaseCodes }
        if (untaken.isEmpty()) {
            return emptyResponse("DOUBLE_MAJOR_REQUIRED", progress, "이번 학기에 수강 가능한 복수전공필수 과목이 없습니다.")
        }
        val courses = buildRecommendedCourses(untaken, ctx.userGrade, field = "복필")
        return CategoryRecommendResponse(
            category = "DOUBLE_MAJOR_REQUIRED",
            progress = progress,
            message = null,
            userGrade = null,
            courses = courses,
            lateFields = null,
        )
    }

    /**
     * 복수전공선택 추천
     * - flat courses, field = "복선", userGrade 포함 → 프론트가 targetGrades로 학년별 그룹
     */
    fun recommendDoubleMajorElective(ctx: RecommendContext): CategoryRecommendResponse {
        val departmentName = ctx.flags.doubleMajorDepartment
            ?: return notRegisteredResponse("DOUBLE_MAJOR_ELECTIVE", "복수전공을 등록하지 않았습니다.")
        val progress = progressOrUnavailable(ctx.graduationSummary?.doubleMajorElective)
        if (progress.satisfied) {
            return satisfiedResponse("DOUBLE_MAJOR_ELECTIVE", progress, "복수전공선택 학점을 이미 모두 이수하셨습니다.")
        }
        val department = departmentReader.getByName(departmentName)
        val coursesWithTarget = courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = SecondaryMajorTrackType.DOUBLE_MAJOR,
            completionType = SecondaryMajorCompletionType.ELECTIVE,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            userGrade = ctx.userGrade,
            maxGrade = MAX_GRADE,
        )
        val takenBaseCodes = toTakenBaseCodeSet(ctx.takenSubjectCodes)
        val untaken = coursesWithTarget.filter { it.course.baseCode() !in takenBaseCodes }
        if (untaken.isEmpty()) {
            return emptyResponse("DOUBLE_MAJOR_ELECTIVE", progress, "이번 학기에 수강 가능한 복수전공선택 과목이 없습니다.")
        }
        val courses = buildRecommendedCourses(untaken, ctx.userGrade, field = "복선")
        return CategoryRecommendResponse(
            category = "DOUBLE_MAJOR_ELECTIVE",
            progress = progress,
            message = null,
            userGrade = ctx.userGrade,
            courses = courses,
            lateFields = null,
        )
    }

    /**
     * 부전공 추천 (부필 + 부선 한 카테고리)
     * - flat courses, field = "부필" | "부선" → 프론트가 부필/부선 그룹핑
     */
    fun recommendMinor(ctx: RecommendContext): CategoryRecommendResponse {
        val departmentName = ctx.flags.minorDepartment
            ?: return notRegisteredResponse("MINOR", "부전공을 등록하지 않았습니다.")
        val progress = progressOrUnavailable(ctx.graduationSummary?.minor)
        if (progress.satisfied) {
            return satisfiedResponse("MINOR", progress, "부전공 학점을 이미 모두 이수하셨습니다.")
        }
        val department = departmentReader.getByName(departmentName)
        val takenBaseCodes = toTakenBaseCodeSet(ctx.takenSubjectCodes)

        val requiredWithTarget = courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = SecondaryMajorTrackType.MINOR,
            completionType = SecondaryMajorCompletionType.REQUIRED,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            userGrade = ctx.userGrade,
            maxGrade = ctx.userGrade,
        ).filter { it.course.baseCode() !in takenBaseCodes }
        val electiveWithTarget = courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = SecondaryMajorTrackType.MINOR,
            completionType = SecondaryMajorCompletionType.ELECTIVE,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            userGrade = ctx.userGrade,
            maxGrade = ctx.userGrade,
        ).filter { it.course.baseCode() !in takenBaseCodes }

        val courses = buildRecommendedCourses(requiredWithTarget, ctx.userGrade, field = "부필") +
            buildRecommendedCourses(electiveWithTarget, ctx.userGrade, field = "부선")
        if (courses.isEmpty()) {
            return emptyResponse("MINOR", progress, "이번 학기에 수강 가능한 부전공 과목이 없습니다.")
        }
        return CategoryRecommendResponse(
            category = "MINOR",
            progress = progress,
            message = null,
            userGrade = null,
            courses = courses,
            lateFields = null,
        )
    }

    private fun buildRecommendedCourses(
        coursesWithTarget: List<CourseWithTarget>,
        userGrade: Int,
        field: String,
    ): List<RecommendedCourseResponse> {
        return coursesWithTarget
            .groupBy { it.course.baseCode() }
            .entries
            .sortedWith(
                compareBy(
                    { if (it.value.first().isLateFor(userGrade)) 0 else 1 },
                    { it.value.first().targetGrades.maxOrNull() ?: 1 },
                    { it.value.first().course.name },
                )
            )
            .map { (_, sections) ->
                val representative = sections.first()
                RecommendedCourseResponse.from(
                    coursesWithTarget = sections,
                    isLate = representative.isLateFor(userGrade),
                    field = field,
                    isCrossMajor = false,
                )
            }
    }

    private fun notRegisteredResponse(category: String, message: String) = CategoryRecommendResponse(
        category = category,
        progress = Progress(required = 0, completed = 0, satisfied = true),
        message = message,
        userGrade = null,
        courses = emptyList(),
        lateFields = null,
    )

    private fun satisfiedResponse(category: String, progress: Progress, message: String) = CategoryRecommendResponse(
        category = category,
        progress = progress,
        message = message,
        userGrade = null,
        courses = emptyList(),
        lateFields = null,
    )

    private fun emptyResponse(category: String, progress: Progress, message: String) = CategoryRecommendResponse(
        category = category,
        progress = progress,
        message = message,
        userGrade = null,
        courses = emptyList(),
        lateFields = null,
    )

    private fun progressOrUnavailable(summaryItem: RusaintCreditSummaryItemDto?): Progress {
        return if (summaryItem != null) {
            Progress.from(summaryItem)
        } else {
            Progress.unavailable()
        }
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
