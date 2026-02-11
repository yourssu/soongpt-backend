package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toTakenBaseCodeSet
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

/**
 * 교양 과목 추천 서비스
 * - 교양필수(GENERAL_REQUIRED), 교양선택(GENERAL_ELECTIVE) 추천
 * - 분야(field)별 그룹핑
 * - 교필: LATE 분야는 텍스트만, ON_TIME 분야는 과목 포함
 * - 교선: 분야별 과목 그룹핑
 */
@Service
class GeneralCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
) {
    private val logger = KotlinLogging.logger {}

    fun recommend(
        category: Category,
        departmentName: String,
        userGrade: Int,
        schoolId: Int,
        admissionYear: Int,
        takenSubjectCodes: List<String>,
        progress: Progress,
    ): CategoryRecommendResponse {
        require(category == Category.GENERAL_REQUIRED || category == Category.GENERAL_ELECTIVE) {
            "Category must be GENERAL_REQUIRED or GENERAL_ELECTIVE"
        }

        if (progress.satisfied) {
            return satisfied(category, progress)
        }

        val department = departmentReader.getByName(departmentName)
        val allCourses = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = userGrade,
        ).distinctBy { it.course.code }

        if (allCourses.isEmpty()) {
            return empty(category, progress)
        }

        val takenBaseCodes = toTakenBaseCodeSet(takenSubjectCodes)

        // 과목별 field 파싱 후 (field → courses) 그룹핑
        val coursesByField = allCourses
            .mapNotNull { cwt ->
                val fieldName = cwt.course.field
                    ?.let { FieldFinder.findFieldBySchoolId(it, schoolId) }
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                Pair(fieldName, cwt)
            }
            .groupBy({ it.first }, { it.second })

        return when (category) {
            Category.GENERAL_REQUIRED -> {
                val is22OrBelow = admissionYear <= 2022
                if (is22OrBelow) {
                    // 22학번 이하: 학점만 채움. 분야 단위 제외 없음, 과목 단위 제외만. lateFields 미제공.
                    val filteredByField = coursesByField.mapValues { (_, courses) ->
                        courses.filter { it.course.baseCode() !in takenBaseCodes }
                    }.filter { (_, courses) -> courses.isNotEmpty() }
                    if (filteredByField.isEmpty()) return empty(category, progress)
                    buildGeneralRequiredResponseFor22(filteredByField, userGrade, progress)
                } else {
                    // 23학번 이상: 이수한 분야 제외, LATE 분야 lateFields, ON_TIME 분야 과목
                    val takenFieldsFromDb = courseRepository.findCoursesWithTargetByBaseCodes(takenBaseCodes.toList())
                        .filter { it.course.category == Category.GENERAL_REQUIRED }
                        .mapNotNull { cwt ->
                            cwt.course.field
                                ?.let { FieldFinder.findFieldBySchoolId(it, schoolId) }
                                ?.takeIf { it.isNotBlank() }
                        }
                        .toSet()
                    val untakenFields = coursesByField.filter { (fieldName, _) -> fieldName !in takenFieldsFromDb }
                    logger.info { "[교필] takenBaseCodes.size=${takenBaseCodes.size}, DB기준 이수분야=$takenFieldsFromDb, 미이수 분야=${untakenFields.keys}" }
                    if (untakenFields.isEmpty()) return empty(category, progress)
                    buildGeneralRequiredResponse(untakenFields, userGrade, progress)
                }
            }
            else -> {
                // 교선: 과목 단위 이수 필터링 — baseCode(8자리) 일치 과목만 개별 제외
                val filteredByField = coursesByField.mapValues { (_, courses) ->
                    courses.filter { it.course.baseCode() !in takenBaseCodes }
                }.filter { (_, courses) -> courses.isNotEmpty() }
                if (filteredByField.isEmpty()) return empty(category, progress)
                buildGeneralElectiveResponse(filteredByField, userGrade, category, progress)
            }
        }
    }

    /**
     * 교양선택 트랙(분야)별 미수강 과목 조회
     *
     * - 이수 필터: baseCode(8자리) 단위 제외 (분야 단위 제외 X — 전공과 동일)
     * - 이미 충족한 트랙(분야 내 이수 과목 1개 이상) → 빈 리스트
     * - 미충족 트랙 → 해당 분야의 미수강 과목 리스트
     *
     * @return 트랙명 → 미수강 Course 리스트 (충족 트랙은 emptyList)
     */
    fun resolveElectiveFields(
        departmentName: String,
        userGrade: Int,
        schoolId: Int,
        takenSubjectCodes: List<String>,
    ): Map<String, List<Course>> {
        val department = departmentReader.getByName(departmentName)
        val allCourses = courseRepository.findCoursesWithTargetByCategory(
            category = Category.GENERAL_ELECTIVE,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = userGrade,
        ).distinctBy { it.course.code }

        if (allCourses.isEmpty()) return emptyMap()

        val takenBaseCodes = toTakenBaseCodeSet(takenSubjectCodes)

        val coursesByField = allCourses
            .mapNotNull { cwt ->
                val fieldName = cwt.course.field
                    ?.let { FieldFinder.findFieldBySchoolId(it, schoolId) }
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                Pair(fieldName, cwt)
            }
            .groupBy({ it.first }, { it.second })

        return coursesByField.mapValues { (_, courses) ->
            val completed = courses.any { it.course.baseCode() in takenBaseCodes }
            if (completed) emptyList() else courses.map { it.course }
        }
    }

    /**
     * 교양필수 22학번 이하: 과목만 flat 반환, lateFields 없음 (분야 구분 없이 학점만 채움)
     */
    private fun buildGeneralRequiredResponseFor22(
        filteredByField: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        progress: Progress,
    ): CategoryRecommendResponse {
        val courses = filteredByField.entries
            .flatMap { (fieldName, courses) -> buildCoursesWithField(fieldName, courses, userGrade) }
        return CategoryRecommendResponse(
            category = Category.GENERAL_REQUIRED.name,
            progress = progress,
            message = null,
            userGrade = null,
            courses = courses,
            lateFields = null,
        )
    }

    /**
     * 교양필수: LATE 분야 → lateFields(텍스트), ON_TIME 분야 → courses(field 포함)
     */
    private fun buildGeneralRequiredResponse(
        untakenFields: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        progress: Progress,
    ): CategoryRecommendResponse {
        val lateFields = mutableListOf<String>()
        val onTimeCourses = mutableListOf<RecommendedCourseResponse>()

        for ((fieldName, courses) in untakenFields) {
            val isFieldLate = courses.all { it.isLateFor(userGrade) }
            if (isFieldLate) {
                lateFields.add(fieldName)
            } else {
                onTimeCourses.addAll(buildCoursesWithField(fieldName, courses, userGrade))
            }
        }

        return CategoryRecommendResponse(
            category = Category.GENERAL_REQUIRED.name,
            progress = progress,
            message = null,
            userGrade = null,
            courses = onTimeCourses,
            lateFields = lateFields.ifEmpty { null },
        )
    }

    /**
     * 교양선택: 분야별 과목을 flat courses 리스트로 반환 (각 항목에 field 포함)
     */
    private fun buildGeneralElectiveResponse(
        untakenFields: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        category: Category,
        progress: Progress,
    ): CategoryRecommendResponse {
        val courses = untakenFields.entries
            .flatMap { (fieldName, courses) -> buildCoursesWithField(fieldName, courses, userGrade) }

        return CategoryRecommendResponse(
            category = category.name,
            progress = progress,
            message = null,
            userGrade = null,
            courses = courses,
            lateFields = null,
        )
    }

    private fun buildCoursesWithField(
        fieldName: String,
        courses: List<CourseWithTarget>,
        userGrade: Int,
    ): List<RecommendedCourseResponse> {
        val grouped = courses.groupBy { it.course.baseCode() }
        return grouped.entries
            .sortedBy { it.value.first().course.name }
            .map { (_, sections) ->
                val representative = sections.first()
                RecommendedCourseResponse.from(
                    coursesWithTarget = sections,
                    isLate = representative.isLateFor(userGrade),
                    field = fieldName,
                )
            }
    }

    private fun satisfied(category: Category, progress: Progress): CategoryRecommendResponse {
        val message = when (category) {
            Category.GENERAL_REQUIRED -> "교양필수 학점을 이미 모두 이수하셨습니다."
            Category.GENERAL_ELECTIVE -> "교양선택 학점을 이미 모두 이수하셨습니다."
            else -> "이미 모두 이수하셨습니다."
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

    private fun empty(category: Category, progress: Progress): CategoryRecommendResponse {
        val message = when (category) {
            Category.GENERAL_REQUIRED -> "이번 학기에 수강 가능한 교양필수 과목이 없습니다."
            Category.GENERAL_ELECTIVE -> "이번 학기에 수강 가능한 교양선택 과목이 없습니다."
            else -> "이번 학기에 수강 가능한 과목이 없습니다."
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
