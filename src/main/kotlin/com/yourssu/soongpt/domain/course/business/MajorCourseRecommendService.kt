package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResult
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toTakenBaseCodeSet
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import org.springframework.stereotype.Service

/**
 * 전공 과목 추천 서비스
 * - 전기(MAJOR_BASIC), 전필(MAJOR_REQUIRED), 전선(MAJOR_ELECTIVE) 추천
 */
@Service
class MajorCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
) {

    /**
     * 전공기초/전공필수 과목 추천
     * - 학년 범위: 1~현재학년
     */
    fun recommendMajorBasicOrRequired(
        departmentName: String,
        userGrade: Int,
        category: Category,
        takenSubjectCodes: List<String>,
        progress: com.yourssu.soongpt.domain.course.business.dto.Progress,
    ): CategoryRecommendResult {
        require(category == Category.MAJOR_BASIC || category == Category.MAJOR_REQUIRED) {
            "Category must be MAJOR_BASIC or MAJOR_REQUIRED"
        }

        if (progress.satisfied) {
            return CategoryRecommendResult.satisfied(category, progress)
        }

        val department = departmentReader.getByName(departmentName)
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = userGrade,
            takenSubjectCodes = takenSubjectCodes,
        )

        if (untakenCourses.isEmpty()) {
            return CategoryRecommendResult.empty(category, progress)
        }

        val recommendedCourses = buildRecommendedCourses(untakenCourses, userGrade)

        return CategoryRecommendResult.of(
            category = category,
            progress = progress,
            courses = recommendedCourses,
        )
    }

    /**
     * 전공선택 과목 추천
     * - 학년 범위: 전체 (1~5학년)
     * - 대상학년 순으로 정렬
     */
    fun recommendMajorElective(
        departmentName: String,
        userGrade: Int,
        takenSubjectCodes: List<String>,
        progress: com.yourssu.soongpt.domain.course.business.dto.Progress,
    ): CategoryRecommendResult {
        val category = Category.MAJOR_ELECTIVE

        if (progress.satisfied) {
            return CategoryRecommendResult.satisfied(category, progress)
        }

        val department = departmentReader.getByName(departmentName)
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = MAX_GRADE,
            takenSubjectCodes = takenSubjectCodes,
        )

        if (untakenCourses.isEmpty()) {
            return CategoryRecommendResult.empty(category, progress)
        }

        val recommendedCourses = buildRecommendedCourses(untakenCourses, userGrade)

        return CategoryRecommendResult.of(
            category = category,
            progress = progress,
            courses = recommendedCourses,
        )
    }

    /**
     * 전공선택 과목 추천
     * - 통합 엔드포인트용: CategoryRecommendResponse 반환
     * - 학년 범위: 전체 (1~5학년)
     */
    fun recommendMajorElectiveWithGroups(
        departmentName: String,
        userGrade: Int,
        takenSubjectCodes: List<String>,
        progress: com.yourssu.soongpt.domain.course.business.dto.Progress,
    ): CategoryRecommendResponse {
        val category = Category.MAJOR_ELECTIVE

        if (progress.satisfied) {
            return CategoryRecommendResponse(
                category = category.name,
                progress = progress,
                message = "전공선택 학점을 이미 모두 이수하셨습니다.",
                userGrade = userGrade,
                courses = emptyList(),
                lateFields = null,
            )
        }

        val department = departmentReader.getByName(departmentName)
        val takenBaseCodes = toTakenBaseCodeSet(takenSubjectCodes)

        // 1) 전공선택 과목 조회 + 이수 과목 제외
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = MAX_GRADE,
            takenSubjectCodes = takenSubjectCodes,
        )

        // 2) 타전공인정 과목 조회 + 이수 과목 제외 + 전선과 baseCode 중복 제거
        val electiveBaseCodes = untakenCourses.map { it.course.baseCode() }.toSet()
        val crossMajorCourses = courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = SecondaryMajorTrackType.CROSS_MAJOR,
            completionType = SecondaryMajorCompletionType.RECOGNIZED,
            departmentId = department.id,
            collegeId = department.collegeId,
            maxGrade = MAX_GRADE,
        ).filter { it.course.baseCode() !in takenBaseCodes }
            .filter { it.course.baseCode() !in electiveBaseCodes }

        if (untakenCourses.isEmpty() && crossMajorCourses.isEmpty()) {
            return CategoryRecommendResponse(
                category = category.name,
                progress = progress,
                message = "이번 학기에 수강 가능한 전공선택 과목이 없습니다.",
                userGrade = userGrade,
                courses = emptyList(),
                lateFields = null,
            )
        }

        // 3) 전선 먼저, 타전공인정 뒤에
        val electiveCourses = buildRecommendedCourses(untakenCourses, userGrade)
        val crossMajorRecommended = buildRecommendedCourses(crossMajorCourses, userGrade, isCrossMajor = true)
        val allCourses = electiveCourses + crossMajorRecommended

        return CategoryRecommendResponse(
            category = category.name,
            progress = progress,
            message = null,
            userGrade = userGrade,
            courses = allCourses,
            lateFields = null,
        )
    }

    /**
     * Target + Course join으로 미수강 과목 조회
     */
    private fun getUntakenCoursesWithTarget(
        category: Category,
        departmentId: Long,
        collegeId: Long,
        maxGrade: Int,
        takenSubjectCodes: List<String>,
    ): List<CourseWithTarget> {
        val coursesWithTarget = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = departmentId,
            collegeId = collegeId,
            maxGrade = maxGrade,
        )

        if (coursesWithTarget.isEmpty()) {
            return emptyList()
        }

        val takenBaseCodes = toTakenBaseCodeSet(takenSubjectCodes)
        return coursesWithTarget.filter { it.course.baseCode() !in takenBaseCodes }
    }

    /**
     * 분반 그룹을 추천 과목 응답으로 변환
     * - 같은 baseCode를 가진 과목들을 그룹핑
     * - Target의 grade 정보로 CourseTiming 판단
     * - 정렬: timing(LATE먼저) → 대상학년 → 과목명
     */
    private fun buildRecommendedCourses(
        coursesWithTarget: List<CourseWithTarget>,
        userGrade: Int,
        isCrossMajor: Boolean = false,
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
                    isCrossMajor = isCrossMajor,
                )
            }
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
