package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.infrastructure.notification.Notification
import com.yourssu.soongpt.common.util.DepartmentNameNormalizer
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

/**
 * Rusaint에서 받은 학생 기본 정보를 검증하고, 매칭 실패 시 Slack 알림을 보냄.
 *
 * 검증 항목:
 * - 학년 (grade): 1~5 범위
 * - 학기 (semester): 1~12 범위
 * - 입학년도 (year): 2015~2026 범위
 * - 학과 (department): DB에 존재하는 학과인지 확인 (DepartmentReader 사용)
 */
@Component
class StudentInfoValidator(
    private val departmentReader: DepartmentReader,
) {
    private val logger = KotlinLogging.logger {}

    data class ValidationResult(
        val isValid: Boolean,
        val failureReason: String? = null,
    )

    /**
     * 학생 기본 정보 유효성 검증.
     * 유효하지 않으면 Slack 알림을 보내고 ValidationResult를 반환.
     */
    fun validate(
        basicInfo: RusaintBasicInfoDto,
        studentIdPrefix: String,
        rawDataForLogging: Map<String, Any?> = emptyMap(),
    ): ValidationResult {
        val errors = mutableListOf<String>()

        // 학년 검증: 1~5
        if (basicInfo.grade !in 1..5) {
            errors.add("학년이 유효하지 않음: ${basicInfo.grade} (예상 범위: 1~5)")
        }

        // 학기 검증: 1~12 범위
        if (basicInfo.semester !in 1..12) {
            errors.add("학기가 유효하지 않음: ${basicInfo.semester} (예상 범위: 1~12)")
        }

        // 입학년도 검증: 2015~2026
        if (basicInfo.year !in 2015..2026) {
            errors.add("입학년도가 유효하지 않음: ${basicInfo.year} (예상 범위: 2015~2026)")
        }

        // 학과 매칭 검증: DB에서 찾을 수 있는지 확인
        val normalizedDepartment = DepartmentNameNormalizer.normalize(basicInfo.department)
        if (normalizedDepartment.isBlank()) {
            errors.add("학과가 비어있음")
        } else {
            // DB에서 학과를 찾아봄
            val department = try {
                departmentReader.getByName(normalizedDepartment)
            } catch (e: Exception) {
                null
            }

            if (department == null) {
                errors.add("학과 매칭 실패: '${basicInfo.department}' (정규화: '$normalizedDepartment', DB에서 찾을 수 없음)")
            }
        }

        if (errors.isNotEmpty()) {
            val failureReason = errors.joinToString(", ")
            logger.warn { "학생 정보 매칭 실패: $studentIdPrefix****, 사유: $failureReason" }

            // 로그 출력 → observer.py가 감지 후 Slack 전송 (기존 방식)
            Notification.notifyStudentInfoMappingFailed(
                studentIdPrefix = studentIdPrefix,
                failureReason = failureReason,
            )

            return ValidationResult(
                isValid = false,
                failureReason = failureReason,
            )
        }

        logger.info { "학생 정보 검증 성공: $studentIdPrefix****, 학년=${basicInfo.grade}, 학과=${basicInfo.department} (정규화: $normalizedDepartment)" }
        return ValidationResult(isValid = true)
    }
}
