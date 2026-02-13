package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintAcademicResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationResponseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import org.springframework.stereotype.Component

/**
 * rusaint academic 스냅샷과 graduation 스냅샷을 병합하여 [RusaintUsaintDataResponse]를 만든다.
 * 단일 책임: 두 응답의 병합 로직만 담당.
 *
 * rusaint 응답은 그대로 전달한다. requirements가 비어 있어도(복수전공 미등록 등), graduationSummary는 항상 사용.
 */
@Component
class RusaintSnapshotMerger {

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

        val warnings = academic.warnings.toMutableList()
        if (!hasGraduation) {
            warnings.add("NO_GRADUATION_DATA")
        }

        return RusaintUsaintDataResponse(
            pseudonym = pseudonym,
            takenCourses = academic.takenCourses,
            lowGradeSubjectCodes = academic.lowGradeSubjectCodes,
            flags = academic.flags,
            basicInfo = academic.basicInfo,
            graduationRequirements = graduation?.graduationRequirements,
            graduationSummary = graduation?.graduationSummary,
            warnings = warnings,
        )
    }
}
