package com.yourssu.soongpt.domain.course.application.support

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintChapelSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintTakenCourseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse

/**
 * 실제 DB 과목 데이터를 조회하여 [RusaintUsaintDataResponse]를 자동 생성하는 빌더.
 * 과목코드 하드코딩 없이, [TakenStrategy]에 따라 기이수 과목을 선별한다.
 * 데이터(26-1→26-2)가 바뀌어도 빌더 로직만 재실행하면 유효한 세션이 만들어진다.
 */
class MockSessionBuilder(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
    private val currentYear: Int = 2025,
    private val currentSemester: Int = 1,
) {
    private var pseudonym: String = "TEST_PSEUDONYM"
    private var departmentName: String = "컴퓨터학부"
    private var admissionYear: Int = 2021
    private var grade: Int = 3
    private var doubleMajorDepartment: String? = null
    private var minorDepartment: String? = null
    private var teaching: Boolean = false
    private var chapelSatisfied: Boolean = false

    private var majorBasicStrategy: TakenStrategy = TakenStrategy.NONE
    private var majorRequiredStrategy: TakenStrategy = TakenStrategy.NONE
    private var majorElectiveStrategy: TakenStrategy = TakenStrategy.NONE
    private var generalRequiredStrategy: TakenStrategy = TakenStrategy.NONE
    private var generalElectiveStrategy: TakenStrategy = TakenStrategy.NONE
    private var doubleMajorRequiredStrategy: TakenStrategy = TakenStrategy.NONE
    private var doubleMajorElectiveStrategy: TakenStrategy = TakenStrategy.NONE

    private var lowGradeSubjectCodes: List<String> = emptyList()
    private var extraTakenCodes: List<String> = emptyList()

    /** rusaint 1-1 또는 졸업사정표 미제공 시: graduationSummary = null, 경고(NO_GRADUATION_REPORT) 처리용 */
    private var noGraduationReport: Boolean = false

    // 졸업요건 학점 (지정하지 않으면 completed 기반으로 자동 설정)
    private var majorFoundationRequired: Int? = null
    private var majorRequiredRequired: Int? = null
    private var majorElectiveRequired: Int? = null
    private var generalRequiredRequired: Int? = null
    private var generalElectiveRequired: Int? = null
    private var doubleMajorRequiredRequired: Int? = null
    private var doubleMajorElectiveRequired: Int? = null
    private var christianCoursesRequired: Int = 6
    private var christianCoursesCompleted: Int = 0

    fun pseudonym(value: String) = apply { pseudonym = value }
    fun department(value: String) = apply { departmentName = value }
    fun admissionYear(value: Int) = apply { admissionYear = value }
    fun grade(value: Int) = apply { grade = value }
    fun doubleMajor(value: String?) = apply { doubleMajorDepartment = value }
    fun minor(value: String?) = apply { minorDepartment = value }
    fun teaching(value: Boolean) = apply { teaching = value }
    fun chapel(satisfied: Boolean) = apply { chapelSatisfied = satisfied }

    fun majorBasic(s: TakenStrategy) = apply { majorBasicStrategy = s }
    fun majorRequired(s: TakenStrategy) = apply { majorRequiredStrategy = s }
    fun majorElective(s: TakenStrategy) = apply { majorElectiveStrategy = s }
    fun generalRequired(s: TakenStrategy) = apply { generalRequiredStrategy = s }
    fun generalElective(s: TakenStrategy) = apply { generalElectiveStrategy = s }
    fun doubleMajorRequired(s: TakenStrategy) = apply { doubleMajorRequiredStrategy = s }
    fun doubleMajorElective(s: TakenStrategy) = apply { doubleMajorElectiveStrategy = s }

    fun retake(baseCodes: List<String>) = apply { lowGradeSubjectCodes = baseCodes }

    /** 전략 자동 생성 외에 추가로 기이수에 포함시킬 과목코드 (하드코딩 과목 테스트용) */
    fun extraTakenCodes(codes: List<String>) = apply { extraTakenCodes = codes }

    /** 졸업사정표 없음(1-1 또는 rusaint 미제공). graduationSummary = null → 경고 NO_GRADUATION_REPORT, 카테고리별 noDataResponse */
    fun noGraduationReport(value: Boolean = true) = apply { noGraduationReport = value }

    /** 해당 이수구분 없음(0/0/true) — 경영 전기, 글통 전필, 회계 전기 등 */
    fun majorFoundationRequiredCredits(value: Int?) = apply { majorFoundationRequired = value }
    fun majorRequiredCredits(value: Int?) = apply { majorRequiredRequired = value }

    fun build(): RusaintUsaintDataResponse {
        val dept = departmentReader.getByName(departmentName)
        val allTakenCodes = mutableListOf<String>()
        val completedCredits = mutableMapOf<String, Int>() // category name -> sum of credits

        fun addTaken(category: Category, courses: List<CourseWithTarget>) {
            val codes = courses.map { it.course.baseCode().toString() }.distinct()
            allTakenCodes.addAll(codes)
            val sum = courses.distinctBy { it.course.baseCode() }.sumOf { (it.course.credit ?: 0).toInt() }
            completedCredits[category.name] = (completedCredits[category.name] ?: 0) + sum
        }

        // 전기/전필/전선/교필/교선 (본인 학과)
        listOf(
            Pair(Category.MAJOR_BASIC, majorBasicStrategy),
            Pair(Category.MAJOR_REQUIRED, majorRequiredStrategy),
            Pair(Category.MAJOR_ELECTIVE, majorElectiveStrategy),
            Pair(Category.GENERAL_REQUIRED, generalRequiredStrategy),
            Pair(Category.GENERAL_ELECTIVE, generalElectiveStrategy),
        ).forEach { (cat, strategy) ->
            if (strategy == TakenStrategy.NONE) return@forEach
            val courses = courseRepository.findCoursesWithTargetByCategory(
                category = cat,
                departmentId = dept.id!!,
                collegeId = dept.collegeId,
                userGrade = grade,
                maxGrade = if (cat == Category.MAJOR_ELECTIVE) 5 else grade,
            ).distinctBy { it.course.baseCode() }
            val selected = applyStrategy(courses, strategy, grade)
            addTaken(cat, selected)
        }

        // 복전 (복필/복선)
        doubleMajorDepartment?.let { dmName ->
            val dmDept = departmentReader.getByName(dmName)
            listOf(
                Pair(SecondaryMajorCompletionType.REQUIRED, doubleMajorRequiredStrategy),
                Pair(SecondaryMajorCompletionType.ELECTIVE, doubleMajorElectiveStrategy),
            ).forEach { (completionType, strategy) ->
                if (strategy == TakenStrategy.NONE) return@forEach
                val courses = courseRepository.findCoursesWithTargetBySecondaryMajor(
                    trackType = SecondaryMajorTrackType.DOUBLE_MAJOR,
                    completionType = completionType,
                    departmentId = dmDept.id!!,
                    collegeId = dmDept.collegeId,
                    userGrade = grade,
                    maxGrade = if (completionType == SecondaryMajorCompletionType.ELECTIVE) 5 else grade,
                ).distinctBy { it.course.baseCode() }
                val selected = applyStrategy(courses, strategy, grade)
                val key = if (completionType == SecondaryMajorCompletionType.REQUIRED) "DOUBLE_MAJOR_REQUIRED" else "DOUBLE_MAJOR_ELECTIVE"
                val codes = selected.map { it.course.baseCode().toString() }.distinct()
                allTakenCodes.addAll(codes)
                val sum = selected.distinctBy { it.course.baseCode() }.sumOf { (it.course.credit ?: 0).toInt() }
                completedCredits[key] = (completedCredits[key] ?: 0) + sum
            }
        }

        // 부전공 (부필/부선) — takenCourses에 포함해 이수로 인식시키려면 해당 과목 baseCode 추가
        minorDepartment?.let { minorName ->
            val minorDept = departmentReader.getByName(minorName)
            listOf(
                SecondaryMajorCompletionType.REQUIRED,
                SecondaryMajorCompletionType.ELECTIVE,
            ).forEach { completionType ->
                val courses = courseRepository.findCoursesWithTargetBySecondaryMajor(
                    trackType = SecondaryMajorTrackType.MINOR,
                    completionType = completionType,
                    departmentId = minorDept.id!!,
                    collegeId = minorDept.collegeId,
                    userGrade = grade,
                    maxGrade = 5,
                ).distinctBy { it.course.baseCode() }
                val selected = applyStrategy(courses, TakenStrategy.PARTIAL_ON_TIME, grade)
                allTakenCodes.addAll(selected.map { it.course.baseCode().toString() }.distinct())
            }
        }

        allTakenCodes.addAll(extraTakenCodes)
        val distinctTaken = allTakenCodes.distinct()
        val takenCourses = distributeBySemester(distinctTaken, admissionYear, currentYear, currentSemester)

        val summary = if (noGraduationReport) null else buildGraduationSummary(completedCredits)
        // rusaint 실제 흐름: RusaintSnapshotMerger가 graduation null 시 warnings에 "NO_GRADUATION_DATA" 추가 후
        // 이 DTO가 그대로 syncSessionStore.updateStatus(..., usaintData)로 세션에 저장됨
        val warnings = if (noGraduationReport) listOf("NO_GRADUATION_DATA") else emptyList()

        return RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = takenCourses,
            lowGradeSubjectCodes = lowGradeSubjectCodes,
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = doubleMajorDepartment,
                minorDepartment = minorDepartment,
                teaching = teaching,
            ),
            basicInfo = RusaintBasicInfoDto(
                year = admissionYear,
                semester = (grade - 1) * 2 + currentSemester,
                grade = grade,
                department = departmentName,
            ),
            graduationRequirements = null,
            graduationSummary = summary,
            warnings = warnings,
        )
    }

    private fun applyStrategy(
        courses: List<CourseWithTarget>,
        strategy: TakenStrategy,
        userGrade: Int,
    ): List<CourseWithTarget> {
        if (strategy == TakenStrategy.NONE || courses.isEmpty()) return emptyList()
        if (strategy == TakenStrategy.ALL) return courses
        val past = courses.filter { it.isLateFor(userGrade) }
        val current = courses.filter { !it.isLateFor(userGrade) }
        return when (strategy) {
            TakenStrategy.NONE -> emptyList()
            TakenStrategy.ALL -> courses
            TakenStrategy.PARTIAL_ON_TIME -> past + current.take((current.size / 2).coerceAtLeast(0))
            TakenStrategy.PARTIAL_LATE -> past.dropLast((past.size / 3).coerceAtLeast(0)) + current.take(1)
            TakenStrategy.MOST -> past + current.dropLast(minOf(2, current.size).coerceIn(0, current.size))
            TakenStrategy.SPECIFIC_CODES -> emptyList()
        }
    }

    private fun generateSemesters(
        admissionYear: Int,
        currentYear: Int,
        currentSemester: Int,
    ): List<Pair<Int, String>> {
        val list = mutableListOf<Pair<Int, String>>()
        var y = admissionYear
        var s = 1
        while (y < currentYear || (y == currentYear && s < currentSemester)) {
            list.add(y to s.toString())
            if (s == 2) {
                y++
                s = 1
            } else {
                s = 2
            }
        }
        return list
    }

    private fun distributeBySemester(
        codes: List<String>,
        admissionYear: Int,
        currentYear: Int,
        currentSemester: Int,
    ): List<RusaintTakenCourseDto> {
        if (codes.isEmpty()) return emptyList()
        val semesters = generateSemesters(admissionYear, currentYear, currentSemester)
        if (semesters.isEmpty()) return listOf(RusaintTakenCourseDto(currentYear, currentSemester.toString(), codes))
        val chunkSize = (codes.size + semesters.size - 1).coerceAtLeast(1) / semesters.size
        val chunks = codes.chunked(chunkSize.coerceAtLeast(1)).take(semesters.size)
        return chunks.zip(semesters) { chunk, (year, sem) ->
            RusaintTakenCourseDto(year = year, semester = sem, subjectCodes = chunk)
        }
    }

    private fun buildGraduationSummary(completedCredits: Map<String, Int>): RusaintGraduationSummaryDto {
        fun item(name: String, defaultRequired: Int): RusaintCreditSummaryItemDto {
            val completed = completedCredits[name] ?: 0
            val required = when (name) {
                "MAJOR_BASIC" -> majorFoundationRequired ?: (completed + 1).coerceAtLeast(0)
                "MAJOR_REQUIRED" -> majorRequiredRequired ?: (completed + 1).coerceAtLeast(0)
                "MAJOR_ELECTIVE" -> majorElectiveRequired ?: (completed + 1).coerceAtLeast(0)
                "GENERAL_REQUIRED" -> generalRequiredRequired ?: (completed + 1).coerceAtLeast(0)
                "GENERAL_ELECTIVE" -> generalElectiveRequired ?: (completed + 1).coerceAtLeast(0)
                "DOUBLE_MAJOR_REQUIRED" -> doubleMajorRequiredRequired ?: (completed + 1).coerceAtLeast(0)
                "DOUBLE_MAJOR_ELECTIVE" -> doubleMajorElectiveRequired ?: if (doubleMajorElectiveStrategy == TakenStrategy.ALL && completed > 0) completed else (completed + 1).coerceAtLeast(0)
                else -> defaultRequired
            }
            // 해당 이수구분 없음 (ex: 경영 전기, 글통 전필, 회계 전기)
            if (required == 0) {
                return RusaintCreditSummaryItemDto(required = 0, completed = 0, satisfied = true)
            }
            return RusaintCreditSummaryItemDto(
                required = required,
                completed = completed,
                satisfied = completed >= required,
            )
        }
        return RusaintGraduationSummaryDto(
            generalRequired = item("GENERAL_REQUIRED", 12),
            generalElective = item("GENERAL_ELECTIVE", 12),
            majorFoundation = item("MAJOR_BASIC", 15),
            majorRequired = item("MAJOR_REQUIRED", 21),
            majorElective = item("MAJOR_ELECTIVE", 30),
            minor = RusaintCreditSummaryItemDto(required = 21, completed = completedCredits["MINOR"] ?: 0, satisfied = (completedCredits["MINOR"] ?: 0) >= 21),
            doubleMajorRequired = item("DOUBLE_MAJOR_REQUIRED", 21),
            doubleMajorElective = item("DOUBLE_MAJOR_ELECTIVE", 21),
            christianCourses = RusaintCreditSummaryItemDto(required = christianCoursesRequired, completed = christianCoursesCompleted, satisfied = christianCoursesCompleted >= christianCoursesRequired),
            chapel = RusaintChapelSummaryItemDto(satisfied = chapelSatisfied),
        )
    }
}
