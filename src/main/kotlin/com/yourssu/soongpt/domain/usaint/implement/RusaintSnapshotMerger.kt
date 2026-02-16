package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

/**
 * rusaint academic 스냅샷과 graduation 스냅샷을 병합하여 [RusaintUsaintDataResponse]를 만든다.
 * 단일 책임: 두 응답의 병합 로직만 담당.
 *
 * rusaint 응답은 그대로 전달한다. requirements가 비어 있어도(복수전공 미등록 등), graduationSummary는 항상 사용.
 */
@Component
class RusaintSnapshotMerger(
    private val studentInfoValidator: StudentInfoValidator,
) {

    private val logger = KotlinLogging.logger {}

    data class MergeResult(
        val data: RusaintUsaintDataResponse?,
        val validationError: String? = null,
    )

    /**
     * 학생 정보 검증 포함하여 병합.
     * 검증 실패 시 data는 반환하고 validationError에 사유 포함 (실패한 것만 null이 아닌 partial 데이터 유지).
     * 입학년도(year)가 비어있거나 유효 범위 밖이면 학번 앞 4자리로 채운 basicInfo로 검증·병합.
     */
    fun mergeWithValidation(
        academic: RusaintAcademicResponseDto,
        graduation: RusaintGraduationResponseDto?,
        studentIdPrefix: String,
    ): MergeResult {
        // 입학년도 비어있거나 유효 범위 밖이면 학번 앞 4자리(입학년도)로 채움 (26학번 등 새내기)
        val basicInfoToUse = fillYearFromStudentIdPrefixIfNeeded(academic.basicInfo, studentIdPrefix)

        // 학생 정보 검증 (채워진 basicInfo 사용)
        val validationResult = studentInfoValidator.validate(
            basicInfo = basicInfoToUse,
            studentIdPrefix = studentIdPrefix,
            rawDataForLogging = mapOf(
                "academic_pseudonym" to academic.pseudonym,
                "graduation_pseudonym" to (graduation?.pseudonym ?: "null"),
                "grade" to academic.basicInfo.grade,
                "semester" to academic.basicInfo.semester,
                "year" to academic.basicInfo.year,
                "department" to academic.basicInfo.department,
                "double_major" to (academic.flags.doubleMajorDepartment ?: "null"),
                "minor" to (academic.flags.minorDepartment ?: "null"),
                "teaching" to academic.flags.teaching,
                "taken_courses_count" to academic.takenCourses.size,
                "has_graduation_data" to (graduation != null),
            )
        )

        val academicWithFilledBasicInfo = academic.copy(basicInfo = basicInfoToUse)
        val data = merge(academicWithFilledBasicInfo, graduation)

        if (!validationResult.isValid) {
            logger.error { "학생 정보 검증 실패: $studentIdPrefix****, 사유: ${validationResult.failureReason}" }
            return MergeResult(
                data = data,
                validationError = validationResult.failureReason,
            )
        }

        return MergeResult(data = data)
    }

    /**
     * 입학년도가 비어있거나 유효 범위(2015..2030) 밖이면 학번 앞 4자리를 입학년도로 사용.
     * 26학번 등 새내기에서 유세인트가 year를 비워서 보내는 경우 대비.
     */
    private fun fillYearFromStudentIdPrefixIfNeeded(
        basicInfo: RusaintBasicInfoDto,
        studentIdPrefix: String,
    ): RusaintBasicInfoDto {
        if (basicInfo.year in 2015..2030) return basicInfo
        val yearFromPrefix = studentIdPrefix.toIntOrNull()?.takeIf { it in 2015..2030 } ?: return basicInfo
        return basicInfo.copy(year = yearFromPrefix)
    }

    fun merge(
        academic: RusaintAcademicResponseDto,
        graduation: RusaintGraduationResponseDto?,
    ): RusaintUsaintDataResponse {
        val hasGraduation = graduation != null
        val pseudonym = if (hasGraduation) {
            academic.pseudonym.ifBlank { graduation!!.pseudonym }
        } else {
            academic.pseudonym
        }

        val summary = graduation?.graduationSummary
        val warnings = academic.warnings.toMutableList()
        if (!hasGraduation) {
            warnings.add("NO_GRADUATION_DATA")
        }
        if (summary?.majorRequiredElectiveCombined == true) {
            warnings.add("MAJOR_REQUIRED_ELECTIVE_COMBINED")
        }

        if (hasGraduation) {
            val ge = summary?.generalElective
            if (ge == null) {
                logger.warn {
                    "merge 직후 generalElective가 null: pseudonym=${pseudonym.take(8)}..., " +
                        "graduationSummary 존재=${summary != null}, Python에서 보냈는데 WAS 역직렬화에서 누락 가능성"
                }
            } else {
                logger.info { "[GE_DEBUG] merge 직후 generalElective 존재: pseudonym=${pseudonym.take(8)}..., required=${ge.required}, completed=${ge.completed}" }
            }
        }

        return RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = academic.takenCourses,
            lowGradeSubjectCodes = academic.lowGradeSubjectCodes,
            flags = academic.flags,
            basicInfo = academic.basicInfo,
            graduationRequirements = graduation?.graduationRequirements,
            graduationSummary = summary,
            warnings = warnings,
        )
    }
}
