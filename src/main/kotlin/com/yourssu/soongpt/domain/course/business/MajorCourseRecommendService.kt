package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorCourseRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
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
    ): MajorCourseRecommendResponse {
        require(category == Category.MAJOR_BASIC || category == Category.MAJOR_REQUIRED) {
            "Category must be MAJOR_BASIC or MAJOR_REQUIRED"
        }

        if (progress.satisfied) {
            return MajorCourseRecommendResponse.satisfied(category, progress)
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
            return MajorCourseRecommendResponse.empty(category, progress)
        }

        val recommendedCourses = buildRecommendedCourses(untakenCourses, userGrade)

        return MajorCourseRecommendResponse.of(
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
    ): MajorCourseRecommendResponse {
        val category = Category.MAJOR_ELECTIVE

        if (progress.satisfied) {
            return MajorCourseRecommendResponse.satisfied(category, progress)
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
            return MajorCourseRecommendResponse.empty(category, progress)
        }

        val recommendedCourses = buildRecommendedCourses(untakenCourses, userGrade)

        return MajorCourseRecommendResponse.of(
            category = category,
            progress = progress,
            courses = recommendedCourses,
        )
    }

    /**
     * 전공선택 과목 추천 (학년별 그룹 포함)
     * - 통합 엔드포인트용: CategoryRecommendResponse + gradeGroups 반환
     * - 학년 범위: 전체 (1~5학년)
     * - gradeGroups: 대상학년별 과목 그룹핑
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
        val untakenCourses = getUntakenCoursesWithTarget(
            category = category,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = MAX_GRADE,
            takenSubjectCodes = takenSubjectCodes,
        )

        if (untakenCourses.isEmpty()) {
            return CategoryRecommendResponse(
                category = category.name,
                progress = progress,
                message = "이번 학기에 수강 가능한 전공선택 과목이 없습니다.",
                userGrade = userGrade,
                courses = emptyList(),
                lateFields = null,
            )
        }

        val allCourses = buildRecommendedCourses(untakenCourses, userGrade)

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
                )
            }
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
