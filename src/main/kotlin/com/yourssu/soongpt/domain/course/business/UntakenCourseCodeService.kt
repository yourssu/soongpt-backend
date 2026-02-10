package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.common.handler.UnauthorizedException
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import org.springframework.stereotype.Service

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
) {

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
        val maxGrade = if (category == Category.MAJOR_ELECTIVE) MAX_GRADE else usaintData.basicInfo.grade

        val coursesWithTarget = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = departmentId,
            collegeId = department.collegeId,
            maxGrade = maxGrade,
        )

        val takenBaseCodes = extractTakenBaseCodes(usaintData)

        return coursesWithTarget
            .filter { it.course.baseCode() !in takenBaseCodes }
            .map { it.course.code }
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
        val schoolId = usaintData.basicInfo.year % 100

        val allCourses = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = departmentId,
            collegeId = department.collegeId,
            maxGrade = usaintData.basicInfo.grade,
        ).distinctBy { it.course.code }

        if (allCourses.isEmpty()) return emptyMap()

        val takenBaseCodes = extractTakenBaseCodes(usaintData)

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

        return syncSessionStore.getUsaintData(p)
            ?: throw UnauthorizedException(message = "세션이 만료되었습니다. SSO 로그인을 다시 진행해 주세요.")
    }

    private fun extractTakenBaseCodes(usaintData: RusaintUsaintDataResponse): Set<Long> {
        return usaintData.takenCourses
            .flatMap { it.subjectCodes }
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
