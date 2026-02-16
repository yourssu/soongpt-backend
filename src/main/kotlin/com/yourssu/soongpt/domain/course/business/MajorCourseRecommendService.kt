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
import com.yourssu.soongpt.domain.target.implement.StudentType
import com.yourssu.soongpt.domain.target.implement.TargetReader
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

/**
 * 전공 과목 추천 서비스
 * - 전기(MAJOR_BASIC), 전필(MAJOR_REQUIRED), 전선(MAJOR_ELECTIVE) 추천
 */
@Service
class MajorCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
) {
    private val logger = KotlinLogging.logger {}

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

        // required > 0 인 경우에만 "다 들음"으로 확정. (0,0,true)는 "해당 없음" 또는 파싱 오류일 수 있어 과목 쿼리 수행
        if (progress.satisfied && progress.required > 0) {
            return CategoryRecommendResult.satisfied(category, progress)
        }

        val department = departmentReader.getByName(departmentName)
        val departmentId = requireNotNull(department.id) {
            "Department ID must not be null for department: ${department.name}"
        }
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = departmentId,
            collegeId = department.collegeId,
            userGrade = userGrade,
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

        // required > 0 인 경우에만 "다 들음"으로 확정. (0,0,true)는 과목 쿼리 수행
        if (progress.satisfied && progress.required > 0) {
            return CategoryRecommendResult.satisfied(category, progress)
        }

        val department = departmentReader.getByName(departmentName)
        val departmentId = requireNotNull(department.id) {
            "Department ID must not be null for department: ${department.name}"
        }
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = departmentId,
            collegeId = department.collegeId,
            userGrade = userGrade,
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

        // required > 0 인 경우에만 "다 들음"으로 확정. (0,0,true)는 과목 쿼리 수행
        if (progress.satisfied && progress.required > 0) {
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
        val departmentId = requireNotNull(department.id) {
            "Department ID must not be null for department: ${department.name}"
        }
        val takenBaseCodes = toTakenBaseCodeSet(takenSubjectCodes)

        // 1) 전공선택 과목 조회 + 이수 과목 제외
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = departmentId,
            collegeId = department.collegeId,
            userGrade = userGrade,
            maxGrade = MAX_GRADE,
            takenSubjectCodes = takenSubjectCodes,
        )

        // 2) 타전공인정 과목 조회 + 이수 과목 제외 + 전선과 baseCode 중복 제거
        // 타전공인정과목은 target 필터를 적용하지 않고 원본 분류 테이블 기준으로 조회
        val electiveBaseCodes = untakenCourses.map { it.course.baseCode() }.toSet()
        val crossMajorCoursesRaw = courseRepository.findCoursesBySecondaryMajorClassification(
            trackType = SecondaryMajorTrackType.CROSS_MAJOR,
            completionType = SecondaryMajorCompletionType.RECOGNIZED,
            departmentId = departmentId,
        )
            .filter { it.baseCode() !in takenBaseCodes }
            .filter { it.baseCode() !in electiveBaseCodes }

        // 각 과목의 실제 target 정보를 조회하여 CourseWithTarget으로 변환
        val crossMajorCourseCodes = crossMajorCoursesRaw.map { it.code }
        val targetsByCourseCode = targetReader.findAllByCodes(crossMajorCourseCodes)

        val crossMajorCourses = crossMajorCoursesRaw.map { course ->
            val targets: List<com.yourssu.soongpt.domain.target.implement.Target> =
                (targetsByCourseCode[course.code] ?: emptyList())
                    .filter { it.studentType == StudentType.GENERAL && !it.isDenied } // Allow target만 사용

            val targetGrades = if (targets.isNotEmpty()) {
                // 같은 course code의 모든 target의 학년 정보를 병합
                targets.flatMap { target ->
                    CourseWithTarget.extractTargetGrades(
                        grade1 = target.grade1,
                        grade2 = target.grade2,
                        grade3 = target.grade3,
                        grade4 = target.grade4,
                        grade5 = target.grade5,
                    )
                }.distinct().sorted()
            } else {
                // target 정보가 없는 경우 전체 학년으로 처리
                logger.warn { "타전공인정 과목(${course.code}: ${course.name})의 target 정보가 없습니다." }
                listOf(1, 2, 3, 4, 5)
            }

            val isStrict = targets.any { it.isStrict }

            CourseWithTarget(
                course = course,
                targetGrades = targetGrades,
                isStrict = isStrict,
            )
        }

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
        userGrade: Int,
        maxGrade: Int,
        takenSubjectCodes: List<String>,
    ): List<CourseWithTarget> {
        val coursesWithTarget = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = departmentId,
            collegeId = collegeId,
            userGrade = userGrade,
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
