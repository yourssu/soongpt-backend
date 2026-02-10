package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.handler.UnauthorizedException
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Service
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

/**
 * 이수구분별 미수강 과목코드(10자리) 조회 서비스
 *
 * 사용법:
 *   val codes = untakenCourseCodeService.getUntakenCourseCodes(Category.MAJOR_REQUIRED)
 *   val fieldMap = untakenCourseCodeService.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)
 */
@Service
class UntakenCourseCodeService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
    private val clientJwtProvider: ClientJwtProvider,
    private val syncSessionStore: SyncSessionStore,
) {

    /**
     * 일반 이수구분용 미수강 과목코드 조회 (전기/전필/전선)
     */
    fun getUntakenCourseCodes(category: Category): List<Long> {
        val usaintData = resolveUsaintData()
        val department = departmentReader.getByName(usaintData.basicInfo.department)
        val maxGrade = if (category == Category.MAJOR_ELECTIVE) MAX_GRADE else usaintData.basicInfo.grade

        val coursesWithTarget = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = department.id!!,
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
     * @return 분야명 → 미수강 10자리 과목코드 리스트
     */
    fun getUntakenCourseCodesByField(category: Category): Map<String, List<Long>> {
        val usaintData = resolveUsaintData()
        val department = departmentReader.getByName(usaintData.basicInfo.department)
        val schoolId = usaintData.basicInfo.year % 100

        val allCourses = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = department.id!!,
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

    private fun currentRequest(): HttpServletRequest {
        val attrs = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw IllegalStateException("HTTP 요청 컨텍스트가 없습니다.")
        return attrs.request
    }

    private fun resolveUsaintData(): RusaintUsaintDataResponse {
        val request = currentRequest()
        val pseudonym = clientJwtProvider.extractPseudonymFromRequest(request)
            .getOrElse { throw UnauthorizedException(message = "재인증이 필요합니다. SSO 로그인을 다시 진행해 주세요.") }

        return syncSessionStore.getUsaintData(pseudonym)
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
