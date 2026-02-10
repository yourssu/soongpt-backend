package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import org.springframework.stereotype.Component

/**
 * rusaint academic 스냅샷과 graduation 스냅샷을 병합하여 [RusaintUsaintDataResponse]를 만든다.
 * 단일 책임: 두 응답의 병합 로직만 담당.
 */
@Component
class RusaintSnapshotMerger {

    fun merge(
        academic: RusaintAcademicResponseDto,
        graduation: RusaintGraduationResponseDto?,
    ): RusaintUsaintDataResponse {
        val hasGraduationData = graduation != null &&
            graduation.graduationRequirements.requirements.isNotEmpty()

        val pseudonym = if (hasGraduationData) {
            academic.pseudonym.ifBlank { graduation.pseudonym }
        } else {
            academic.pseudonym
        }

        val warnings = academic.warnings.toMutableList()
        if (!hasGraduationData) {
            warnings.add("NO_GRADUATION_DATA")
        }

        return RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = academic.takenCourses,
            lowGradeSubjectCodes = academic.lowGradeSubjectCodes,
            flags = academic.flags,
            basicInfo = academic.basicInfo,
            graduationRequirements = if (hasGraduationData) graduation.graduationRequirements else null,
            graduationSummary = if (hasGraduationData) graduation.graduationSummary else null,
            warnings = warnings,
        )
    }
}
