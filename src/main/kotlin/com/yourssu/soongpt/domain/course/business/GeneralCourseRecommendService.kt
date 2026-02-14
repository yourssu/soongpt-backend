package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toBaseCode
import com.yourssu.soongpt.domain.course.implement.toTakenBaseCodeSet
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.course.implement.utils.GeneralElectiveFieldDisplayMapper
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

/**
 * 교양 과목 추천 서비스
 * - 교양필수(GENERAL_REQUIRED), 교양선택(GENERAL_ELECTIVE) 추천
 * - 분야(field)별 그룹핑
 * - 교필: LATE 분야는 텍스트만, ON_TIME 분야는 과목 포함
 * - 교선: 분야별 과목 그룹핑
 *
 * 교필 LATE/ON_TIME 판단: target 테이블의 grade1~5는 학과별 "수강 허용 학년"을 나타내며,
 * 교필 분야별 "권장 수강 학년"과 다를 수 있음. (예: 인문적상상력과소통은 1학년 권장이지만
 * target이 전체학년이면 targetGrades=[1,2,3,4,5]가 됨). 따라서 교필은 분야별 고정 매핑 사용.
 */
@Service
class GeneralCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
    private val courseFieldReader: CourseFieldReader,
    private val courseService: CourseService,
) {
    private val logger = KotlinLogging.logger {}

    /**
     * 교필 분야별 권장 수강 학년 (23학번 이상 기준). userGrade가 이 학년보다 크면 LATE.
     * @see src/main/resources/api/requirements/교양필수.md
     * @see src/main/resources/guide/untaken_codes_usage.md
     */
    private val generalRequiredFieldTargetGrade: Map<String, Int> = mapOf(
        "인문적상상력과소통" to 1,
        "비판적사고와표현" to 1,
        "인간과성서" to 1,
        "한반도평화와통일" to 1,
        "컴퓨팅적사고" to 1,
        "SW와AI" to 1,
        "글로벌시민의식" to 2,
        "글로벌소통과언어" to 2,
        "창의적사고와혁신" to 3,
    )

    /** 교필 분야 파싱 시 23학번 기준 사용 (23이후 분야명으로 통일) */
    private val generalRequiredSchoolId = 23

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

        // 교필: 23이후 분야명으로 통일. 교선: 사용자 학번 기준.
        val fieldSchoolId = if (category == Category.GENERAL_REQUIRED) generalRequiredSchoolId else schoolId

        // 과목별 field 파싱 후 (field → courses) 그룹핑
        val coursesByField = allCourses
            .mapNotNull { cwt ->
                val fieldName = cwt.course.field
                    ?.let { FieldFinder.findFieldBySchoolId(it, fieldSchoolId) }
                    ?.takeIf { it.isNotBlank() }
                    ?: run {
                        logger.warn { "분야 정보 없음: 과목=${cwt.course.name} (${cwt.course.code})" }
                        return@mapNotNull null
                    }
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
                                ?.let { FieldFinder.findFieldBySchoolId(it, fieldSchoolId) }
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
                buildGeneralElectiveResponse(filteredByField, userGrade, category, progress, admissionYear)
            }
        }
    }

    /**
     * 교양선택 이수 과목의 분야별 이수 학점 계산
     *
     * 분야 매핑은 field-by-code(CourseService.getFieldByCourseCode)와 동일하게 활용.
     * 학점은 Course 테이블에 있을 때만 합산, 미개설 과목은 해당 분야에 0학점으로 반영.
     *
     * @param takenSubjectCodes 기이수 과목 코드 리스트 (10자리 또는 8자리)
     * @param schoolId 학번 (22, 23 등)
     * @return 분야명 → 이수 학점 Map
     */
    fun computeTakenFieldCredits(takenSubjectCodes: List<String>, schoolId: Int): Map<String, Int> {
        val uniqueBaseCodes = takenSubjectCodes
            .mapNotNull { it.toLongOrNull() }
            .map { it.toBaseCode() }
            .distinct()
        if (uniqueBaseCodes.isEmpty()) return emptyMap()

        val takenCoursesByBase = courseRepository.findCoursesWithTargetByBaseCodes(uniqueBaseCodes.toList())
            .filter { it.course.category == Category.GENERAL_ELECTIVE }
            .distinctBy { it.course.baseCode() }
            .associateBy { it.course.baseCode() }

        val result = mutableMapOf<String, Int>()
        for (baseCode in uniqueBaseCodes) {
            val fieldName = courseService.getFieldByCourseCode(baseCode, schoolId)?.takeIf { it.isNotBlank() }
                ?: continue
            val credit = takenCoursesByBase[baseCode]?.course?.credit?.toInt() ?: 0
            result[fieldName] = result.getOrDefault(fieldName, 0) + credit
        }
        return result
    }

    /**
     * 교양선택 이수 과목의 분야별 이수 과목 수 계산
     *
     * /api/courses/field-by-code와 동일한 로직을 활용: 학번 + 기이수과목코드로 코드→필드 조회 후
     * 분야별로 몇 과목 들었는지 집계. 이번 학기 개설과목뿐 아니라 예전 데이터(course_field)까지 모두 조회.
     *
     * @param takenSubjectCodes 기이수 과목 코드 리스트 (user session, 10자리 또는 8자리)
     * @param schoolId 학번 (22, 23 등)
     * @return 분야명(raw) → 이수 과목 수 Map
     */
    fun computeTakenFieldCourseCounts(takenSubjectCodes: List<String>, schoolId: Int): Map<String, Int> {
        val fieldToBaseCodes = mutableMapOf<String, MutableSet<Long>>()
        for (codeStr in takenSubjectCodes) {
            val codeLong = codeStr.toLongOrNull() ?: continue
            val baseCode = codeLong.toBaseCode()
            // 하드코딩 과목(2150180801): DB field(Bridge교과/수리) 대신 올바른 분야로 override
            val fieldName = if (codeStr == GeneralElectiveFieldDisplayMapper.SCIENCE_HARDCODED_COURSE_CODE) {
                GeneralElectiveFieldDisplayMapper.scienceFieldRawOverride(schoolId)
                    ?: courseService.getFieldByCourseCode(codeLong, schoolId)?.takeIf { it.isNotBlank() }
                    ?: continue
            } else {
                courseService.getFieldByCourseCode(codeLong, schoolId)?.takeIf { it.isNotBlank() }
                    ?: continue
            }
            fieldToBaseCodes.getOrPut(fieldName) { mutableSetOf() }.add(baseCode)
        }
        return fieldToBaseCodes.mapValues { it.value.size }
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
                    ?: run {
                        logger.warn { "분야 정보 없음: 과목=${cwt.course.name} (${cwt.course.code})" }
                        return@mapNotNull null
                    }
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
     * LATE 판단: 분야별 권장 학년(generalRequiredFieldTargetGrade) 기준. userGrade > 권장학년 이면 LATE.
     */
    private fun buildGeneralRequiredResponse(
        untakenFields: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        progress: Progress,
    ): CategoryRecommendResponse {
        val lateFields = mutableListOf<String>()
        val onTimeCourses = mutableListOf<RecommendedCourseResponse>()

        for ((fieldName, courses) in untakenFields) {
            val targetGrade = generalRequiredFieldTargetGrade[fieldName] ?: 1
            val isFieldLate = userGrade > targetGrade
            if (isFieldLate) {
                lateFields.add(fieldName)
            } else {
                onTimeCourses.addAll(buildCoursesWithFieldForGeneralRequired(fieldName, courses, userGrade))
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
     * 교필 ON_TIME 분야용: isLate는 분야별 권장 학년 기준으로 판단 (target 테이블 무시)
     */
    private fun buildCoursesWithFieldForGeneralRequired(
        fieldName: String,
        courses: List<CourseWithTarget>,
        userGrade: Int,
    ): List<RecommendedCourseResponse> {
        val targetGrade = generalRequiredFieldTargetGrade[fieldName] ?: 1
        val isLate = userGrade > targetGrade
        val grouped = courses.groupBy { it.course.baseCode() }
        return grouped.entries
            .sortedBy { it.value.first().course.name }
            .map { (_, sections) ->
                RecommendedCourseResponse.from(
                    coursesWithTarget = sections,
                    isLate = isLate,
                    field = fieldName,
                )
            }
    }

    /**
     * 교양선택: 분야별 과목을 flat courses 리스트로 반환 (각 항목에 field 포함).
     * field는 학번별 표시용(B)으로 매핑하여 전달.
     */
    private fun buildGeneralElectiveResponse(
        untakenFields: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        category: Category,
        progress: Progress,
        admissionYear: Int,
    ): CategoryRecommendResponse {
        val courses = untakenFields.entries
            .flatMap { (rawFieldName, courses) ->
                val displayField = GeneralElectiveFieldDisplayMapper.mapForCourseField(rawFieldName, admissionYear)
                buildCoursesWithField(displayField, courses, userGrade)
            }

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
