package com.yourssu.soongpt.domain.usaint.business

import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.business.dto.UsaintSyncResponse
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintLowGradeSubjectCodesDto
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UsaintService(
    private val pseudonymGenerator: PseudonymGenerator,
    private val rusaintServiceClient: RusaintServiceClient,
    private val courseRepository: CourseRepository,
) {

    private val logger = KotlinLogging.logger {}

    /**
     * u-saint 데이터 동기화 플로우의 진입점.
     *
     * 1) 학번 기반 pseudonym 생성
     * 2) rusaint-service two-track 호출 (academic → 0.5초 후 graduation) 후 병합
     * 3) 저성적 과목 코드를 Course DB 기반으로 이수구분 분류
     * 4) (추후) 응답 기반 DB 저장 및 캐싱
     */
    @Transactional
    fun syncUsaintData(
        request: UsaintSyncRequest,
    ): UsaintSyncResponse {
        val pseudonym = pseudonymGenerator.generate(request.studentId)

        val usaintSnapshot = rusaintServiceClient.syncUsaintData(
            studentId = request.studentId,
            sToken = request.sToken,
        )

        val classifiedLowGrades = classifyLowGradeSubjectCodes(usaintSnapshot.lowGradeSubjectCodes)
        logger.info { "유세인트 데이터 동기화 완료: pseudonym=$pseudonym" }
        logger.debug { "분류 결과 - C/D: ${classifiedLowGrades.passLow}, F: ${classifiedLowGrades.fail}" }

        // TODO: usaintSnapshot + classifiedLowGrades를 pseudonym 기준 DB 저장 (sync/refresh는 나중에)

        return UsaintSyncResponse(
            summary = "usaint data synced",
        )
    }

    /**
     * 저성적 과목 코드를 Course DB 기반으로 이수구분별로 분류.
     */
    private fun classifyLowGradeSubjectCodes(
        lowGrades: RusaintLowGradeSubjectCodesDto,
    ): ClassifiedLowGradeSubjectCodes {
        val passLowCodes = lowGrades.passLow.mapNotNull { it.toLongOrNull() }
        val failCodes = lowGrades.fail.mapNotNull { it.toLongOrNull() }

        val passLowGrouped = if (passLowCodes.isNotEmpty()) {
            courseRepository.groupByCategory(passLowCodes)
        } else {
            emptyGroupedCourses()
        }
        val failGrouped = if (failCodes.isNotEmpty()) {
            courseRepository.groupByCategory(failCodes)
        } else {
            emptyGroupedCourses()
        }

        return ClassifiedLowGradeSubjectCodes(
            passLow = GradeBandSubjectCodes(
                majorRequired = passLowGrouped.majorRequiredCourses.map { it.code.toString() },
                majorElective = passLowGrouped.majorElectiveCourses.map { it.code.toString() },
                generalRequired = passLowGrouped.generalRequiredCourses.map { it.code.toString() },
                generalElective = passLowGrouped.generalElectiveCourses.map { it.code.toString() },
            ),
            fail = GradeBandSubjectCodes(
                majorRequired = failGrouped.majorRequiredCourses.map { it.code.toString() },
                majorElective = failGrouped.majorElectiveCourses.map { it.code.toString() },
                generalRequired = failGrouped.generalRequiredCourses.map { it.code.toString() },
                generalElective = failGrouped.generalElectiveCourses.map { it.code.toString() },
            ),
        )
    }

    private fun emptyGroupedCourses(): GroupedCoursesByCategoryDto =
        GroupedCoursesByCategoryDto(
            majorRequiredCourses = emptyList(),
            majorElectiveCourses = emptyList(),
            generalRequiredCourses = emptyList(),
            generalElectiveCourses = emptyList(),
        )
}

/** 이수구분별로 분류된 저성적 과목 코드. */
data class ClassifiedLowGradeSubjectCodes(
    val passLow: GradeBandSubjectCodes,
    val fail: GradeBandSubjectCodes,
)

/** 이수구분별 과목 코드 목록. */
data class GradeBandSubjectCodes(
    val majorRequired: List<String>,
    val majorElective: List<String>,
    val generalRequired: List<String>,
    val generalElective: List<String>,
)
