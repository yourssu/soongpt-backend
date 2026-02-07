package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.GradeGroupResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorCourseRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.baseCode
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
        progress: String? = null,
        satisfied: Boolean = false,
    ): MajorCourseRecommendResponse {
        require(category == Category.MAJOR_BASIC || category == Category.MAJOR_REQUIRED) {
            "Category must be MAJOR_BASIC or MAJOR_REQUIRED"
        }

        if (satisfied) {
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
            satisfied = false,
            courses = recommendedCourses,
        )
    }

    /**
     * 전공선택 과목 추천
     * - 학년 범위: 전체 (1~5학년)
     * - 학년별 그룹핑 포함
     */
    fun recommendMajorElective(
        departmentName: String,
        userGrade: Int,
        takenSubjectCodes: List<String>,
        progress: String? = null,
        satisfied: Boolean = false,
    ): MajorCourseRecommendResponse {
        val category = Category.MAJOR_ELECTIVE

        if (satisfied) {
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
        val gradeGroups = buildGradeGroups(recommendedCourses)

        return MajorCourseRecommendResponse.of(
            category = category,
            progress = progress,
            satisfied = false,
            courses = recommendedCourses,
            gradeGroups = gradeGroups,
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

        val takenBaseCodes = takenSubjectCodes.map { it.toLong() }.toSet()
        return coursesWithTarget.filter { it.course.baseCode() !in takenBaseCodes }
    }

    /**
     * 분반 그룹을 추천 과목 응답으로 변환
     * - 같은 baseCode를 가진 과목들을 그룹핑
     * - Target의 grade 정보로 CourseTiming 판단
     */
    private fun buildRecommendedCourses(
        coursesWithTarget: List<CourseWithTarget>,
        userGrade: Int,
    ): List<RecommendedCourseResponse> {
        return coursesWithTarget
            .groupBy { it.course.baseCode() }
            .map { (_, sections) ->
                val representative = sections.first()
                RecommendedCourseResponse.from(
                    courses = sections.map { it.course },
                    targetGrades = representative.targetGrades,
                    isLate = representative.isLateFor(userGrade),
                )
            }
            .sortedWith(
                compareBy(
                    { it.timing.ordinal },
                    { it.targetGrades.maxOrNull() ?: 1 },
                    { it.courseName },
                )
            )
    }

    /**
     * 학년별 그룹핑 (전선용)
     * - targetGrades의 최대값 기준으로 그룹핑
     */
    private fun buildGradeGroups(courses: List<RecommendedCourseResponse>): List<GradeGroupResponse> {
        return courses
            .groupBy { it.targetGrades.maxOrNull() ?: 1 }
            .map { (grade, groupCourses) ->
                GradeGroupResponse(grade = grade, courses = groupCourses)
            }
            .sortedBy { it.grade }
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
