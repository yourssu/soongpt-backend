package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.GradeGroupResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorCourseRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseGrouper
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.TakenCoursesFilter
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service

/**
 * 전공 과목 추천 서비스
 * - 전기(MAJOR_BASIC), 전필(MAJOR_REQUIRED), 전선(MAJOR_ELECTIVE) 추천
 */
@Service
class MajorCourseRecommendService(
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
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
        val untakenCourses = getUntakenCourses(department, userGrade, category, takenSubjectCodes)

        if (untakenCourses.isEmpty()) {
            return MajorCourseRecommendResponse.empty(category, progress)
        }

        val groupedCourses = CourseGrouper.groupByBaseCode(untakenCourses)
        val recommendedCourses = buildRecommendedCourses(groupedCourses, userGrade)

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
        val untakenCourses = getUntakenCourses(department, userGrade, category, takenSubjectCodes)

        if (untakenCourses.isEmpty()) {
            return MajorCourseRecommendResponse.empty(category, progress)
        }

        val groupedCourses = CourseGrouper.groupByBaseCode(untakenCourses)
        val recommendedCourses = buildRecommendedCourses(groupedCourses, userGrade)

        // 전선은 학년별 그룹핑 추가
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
     * 개설 과목 중 미수강 과목 조회
     */
    private fun getUntakenCourses(
        department: Department,
        userGrade: Int,
        category: Category,
        takenSubjectCodes: List<String>,
    ): List<Course> {
        // 1. 카테고리별 학년 범위로 과목 코드 조회
        val courseCodes = targetReader.findCourseCodesByCategory(department, userGrade, category)

        if (courseCodes.isEmpty()) {
            return emptyList()
        }

        // 2. 과목 코드로 과목 정보 조회
        val courses = courseReader.findAllInCategory(category, courseCodes, DEFAULT_SCHOOL_ID)

        // 3. 이미 수강한 과목 제외
        val takenBaseCodes = TakenCoursesFilter.extractBaseCodes(takenSubjectCodes)
        return TakenCoursesFilter.excludeTakenCourses(courses, takenBaseCodes)
    }

    /**
     * 분반 그룹을 추천 과목 응답으로 변환
     */
    private fun buildRecommendedCourses(
        groupedCourses: Map<Long, List<Course>>,
        userGrade: Int,
    ): List<RecommendedCourseResponse> {
        return groupedCourses.map { (_, sections) ->
            val targetGrade = extractTargetGrade(sections.first())
            RecommendedCourseResponse.from(sections, targetGrade, userGrade)
        }.sortedWith(
            compareBy(
                { it.status.ordinal }, // PAST_DUE 먼저
                { it.targetGrade },     // 낮은 학년 먼저
                { it.courseName }       // 이름순
            )
        )
    }

    /**
     * 학년별 그룹핑 (전선용)
     */
    private fun buildGradeGroups(courses: List<RecommendedCourseResponse>): List<GradeGroupResponse> {
        return courses
            .groupBy { it.targetGrade }
            .map { (grade, groupCourses) ->
                GradeGroupResponse(grade = grade, courses = groupCourses)
            }
            .sortedBy { it.grade }
    }

    /**
     * 과목의 권장 이수 학년 추출
     * target 문자열에서 학년 정보 파싱
     */
    private fun extractTargetGrade(course: Course): Int {
        val targetText = course.target
        // "1학년", "2학년" 등의 패턴에서 숫자 추출
        val gradeMatch = Regex("(\\d)학년").find(targetText)
        return gradeMatch?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }

    companion object {
        private const val DEFAULT_SCHOOL_ID = 1
    }
}
