package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.InvalidSecondaryMajorFilterException

enum class SecondaryMajorTrackType(
    val displayName: String,
) {
    DOUBLE_MAJOR("복수전공"),
    MINOR("부전공"),
    CROSS_MAJOR("타전공인정"),
    ;

    fun classificationLabel(completionType: SecondaryMajorCompletionType): String {
        return when (this) {
            DOUBLE_MAJOR -> when (completionType) {
                SecondaryMajorCompletionType.REQUIRED -> "복필"
                SecondaryMajorCompletionType.ELECTIVE -> "복선"
                SecondaryMajorCompletionType.RECOGNIZED -> throw InvalidSecondaryMajorFilterException("복수전공은 타전공인정 이수구분을 지원하지 않습니다.")
            }
            MINOR -> when (completionType) {
                SecondaryMajorCompletionType.REQUIRED -> "부필"
                SecondaryMajorCompletionType.ELECTIVE -> "부선"
                SecondaryMajorCompletionType.RECOGNIZED -> throw InvalidSecondaryMajorFilterException("부전공은 타전공인정 이수구분을 지원하지 않습니다.")
            }
            CROSS_MAJOR -> when (completionType) {
                SecondaryMajorCompletionType.RECOGNIZED -> "타전공인정과목"
                else -> throw InvalidSecondaryMajorFilterException("타전공인정은 RECOGNIZED 이수구분만 지원합니다.")
            }
        }
    }

    companion object {
        fun from(value: String): SecondaryMajorTrackType {
            val normalized = value.trim()
            return when (normalized) {
                "DOUBLE_MAJOR", "복수전공", "복전" -> DOUBLE_MAJOR
                "MINOR", "부전공", "부전" -> MINOR
                "CROSS_MAJOR", "타전공", "타전공인정", "타전공인정과목" -> CROSS_MAJOR
                else -> throw InvalidSecondaryMajorFilterException("trackType 값이 올바르지 않습니다: $value")
            }
        }
    }
}
