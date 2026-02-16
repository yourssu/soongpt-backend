package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.common.handler.ForbiddenException
import com.yourssu.soongpt.common.handler.UnauthorizedException
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toTakenBaseCodeSet
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.course.implement.utils.GeneralElectiveFieldDisplayMapper
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.sso.implement.SyncStatus
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * 이수구분별 미수강 과목코드(10자리) 조회 서비스
 *
 * pseudonym은 [CurrentPseudonymHolder]에서 조회한다. HTTP 요청 시 [CurrentPseudonymFilter]가 세팅하므로
 * 컨트롤러·다른 서비스는 인자를 넘기지 않아도 된다.
 *
 * 사용법:
 *   val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
 *   val fieldMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
 *
 * 비동기/스케줄러 등 HTTP 요청이 아닌 경우: 호출 전 [CurrentPseudonymHolder.set]으로 세팅하거나,
 * 오버로드 메서드에 pseudonym 인자를 넘긴다.
 */
@Service
class UntakenCourseCodeService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
    private val syncSessionStore: SyncSessionStore,
    private val courseFieldReader: CourseFieldReader,
) {
    private val logger = KotlinLogging.logger {}
    private val coursesWithTargetCache = ConcurrentHashMap<CoursesWithTargetKey, List<CourseWithTarget>>()

    /**
     * 일반 이수구분용 미수강 과목코드 조회 (전기/전필/전선)
     *
     * @param pseudonym HTTP 요청이 아닌 컨텍스트(비동기 등)에서 호출할 때만 넘긴다. null이면 [CurrentPseudonymHolder] 사용.
     */
    fun getUntakenCourseCodes(category: Category, pseudonym: String? = null): List<Long> {
        val usaintData = resolveUsaintData(pseudonym)
        val department = departmentReader.getByName(usaintData.basicInfo.department)
        val departmentId = department.id
            ?: throw IllegalStateException("학과 ID가 없습니다: ${department.name}")
        val userGrade = usaintData.basicInfo.grade
        val maxGrade = if (category == Category.MAJOR_ELECTIVE) MAX_GRADE else userGrade

        val coursesWithTarget = getCoursesWithTarget(category, departmentId, department.collegeId, userGrade, maxGrade)
        val takenBaseCodes = extractTakenBaseCodes(usaintData)

        return if (category == Category.CHAPEL) {
            coursesWithTarget.map { it.course.code }
        } else {
            coursesWithTarget
                .filter { it.course.baseCode() !in takenBaseCodes }
                .map { it.course.code }
        }
    }

    /**
     * 교양용 미수강 과목코드 조회 (교필/교선 — 분야별 그룹핑)
     *
     * @param pseudonym HTTP 요청이 아닌 컨텍스트(비동기 등)에서 호출할 때만 넘긴다. null이면 [CurrentPseudonymHolder] 사용.
     * @return 분야명 → 미수강 10자리 과목코드 리스트
     */
    fun getUntakenCourseCodesByField(category: Category, pseudonym: String? = null): Map<String, List<Long>> {
        val usaintData = resolveUsaintData(pseudonym)
        val department = departmentReader.getByName(usaintData.basicInfo.department)
        val departmentId = department.id
            ?: throw IllegalStateException("학과 ID가 없습니다: ${department.name}")
        val studentSchoolId = usaintData.basicInfo.year % 100
        // 교필은 23이후 분야명(인문적상상력과소통, SW와AI 등)으로 통일. 교선은 학번별 분야 사용.
        val schoolId = if (category == Category.GENERAL_REQUIRED) GENERAL_REQUIRED_SCHOOL_ID else studentSchoolId

        val allCourses = getCoursesWithTarget(
            category = category,
            departmentId = departmentId,
            collegeId = department.collegeId,
            userGrade = usaintData.basicInfo.grade,
            maxGrade = usaintData.basicInfo.grade,
        )

        if (allCourses.isEmpty()) return emptyMap()

        val takenBaseCodes = extractTakenBaseCodes(usaintData)
        val courseFieldMap = courseFieldReader.findAll().associateBy { it.courseCode }

        // 분야 매핑은 /api/courses/field-by-code와 동일하게 course_field 테이블 사용 (학번별 분야 반영)
        val coursesByField = allCourses
            .mapNotNull { cwt ->
                val rawField = courseFieldMap[cwt.course.code]?.field
                    ?: courseFieldMap[cwt.course.baseCode()]?.field
                    ?: cwt.course.field
                if (rawField.isNullOrBlank()) {
                    logger.warn { "분야 정보 없음: 과목=${cwt.course.name} (${cwt.course.code})" }
                    return@mapNotNull null
                }

                // 하드코딩 과목(2150180801): FieldFinder 결과("수리·물리·화학·생물") 대신 올바른 트랙명 사용
                val fieldName = if (cwt.course.baseCode() == GeneralElectiveFieldDisplayMapper.SCIENCE_HARDCODED_COURSE_CODE.toLong() / 100) {
                    GeneralElectiveFieldDisplayMapper.scienceFieldForCourseDisplay(usaintData.basicInfo.year)
                } else {
                    FieldFinder.findFieldBySchoolId(rawField, schoolId)
                }
                if (fieldName.isBlank()) {
                    logger.warn { "분야 매핑 실패: rawField=$rawField, schoolId=$schoolId, 과목=${cwt.course.name} (${cwt.course.code})" }
                    return@mapNotNull null
                }

                Pair(fieldName, cwt)
            }
            .groupBy({ it.first }, { it.second })

        return when (category) {
            Category.GENERAL_REQUIRED -> {
                coursesByField.mapValues { (_, courses) ->
                    val taken = courses.any { it.course.baseCode() in takenBaseCodes }
                    if (taken) emptyList() else courses.map { it.course.code }
                }
            }
            else -> {
                coursesByField.mapValues { (_, courses) ->
                    courses
                        .filter { it.course.baseCode() !in takenBaseCodes }
                        .map { it.course.code }
                }
            }
        }
    }

    private fun resolveUsaintData(pseudonym: String? = null): RusaintUsaintDataResponse {
        val p = pseudonym ?: CurrentPseudonymHolder.get()
            ?: throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.")

        val usaintData = syncSessionStore.getUsaintData(p)
        if (usaintData != null) return usaintData

        val session = syncSessionStore.getSession(p)
        if (session?.status == SyncStatus.REQUIRES_USER_INPUT) {
            throw ForbiddenException(message = "학생 정보 확인이 필요합니다. SSO 동기화 상태를 확인해 주세요.")
        }
        throw UnauthorizedException(message = "세션이 만료되었습니다. SSO 로그인을 다시 진행해 주세요.")
    }

    private fun extractTakenBaseCodes(usaintData: RusaintUsaintDataResponse): Set<Long> {
        return toTakenBaseCodeSet(usaintData.takenCourses.flatMap { it.subjectCodes })
    }

    private fun getCoursesWithTarget(
        category: Category,
        departmentId: Long,
        collegeId: Long,
        userGrade: Int,
        maxGrade: Int,
    ): List<CourseWithTarget> {
        val key = CoursesWithTargetKey(category, departmentId, collegeId, userGrade, maxGrade)
        return coursesWithTargetCache.getOrPut(key) {
            courseRepository.findCoursesWithTargetByCategory(
                category = category,
                departmentId = departmentId,
                collegeId = collegeId,
                userGrade = userGrade,
                maxGrade = maxGrade,
            ).distinctBy { it.course.code }
        }
    }

    private data class CoursesWithTargetKey(
        val category: Category,
        val departmentId: Long,
        val collegeId: Long,
        val userGrade: Int,
        val maxGrade: Int,
    )

    companion object {
        private const val MAX_GRADE = 5
        /** 교필 분야명을 23이후 기준으로 통일하기 위한 고정 schoolId */
        private const val GENERAL_REQUIRED_SCHOOL_ID = 23
    }
}
