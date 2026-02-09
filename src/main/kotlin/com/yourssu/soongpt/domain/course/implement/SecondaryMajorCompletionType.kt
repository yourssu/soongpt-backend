package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.exception.InvalidSecondaryMajorFilterException

enum class SecondaryMajorCompletionType(
    val displayName: String,
) {
    REQUIRED("필수"),
    ELECTIVE("선택"),
    RECOGNIZED("타전공인정"),
    ;

    companion object {
        fun from(value: String): SecondaryMajorCompletionType {
            val normalized = value.trim()
            return when (normalized) {
                "REQUIRED", "필수" -> REQUIRED
                "ELECTIVE", "선택" -> ELECTIVE
                "RECOGNIZED", "타전공인정", "타전공인정과목" -> RECOGNIZED
                "복필", "부필" -> REQUIRED
                "복선", "부선" -> ELECTIVE
                else -> throw InvalidSecondaryMajorFilterException("completionType 값이 올바르지 않습니다: $value")
            }
        }

        fun validateCompatibility(
            trackType: SecondaryMajorTrackType,
            completionType: SecondaryMajorCompletionType,
            rawCompletionType: String,
        ) {
            val normalized = rawCompletionType.trim()

            when (trackType) {
                SecondaryMajorTrackType.DOUBLE_MAJOR -> {
                    if (completionType == RECOGNIZED) {
                        throw InvalidSecondaryMajorFilterException("복수전공은 RECOGNIZED 이수구분을 지원하지 않습니다.")
                    }
                    if (normalized == "부필" || normalized == "부선") {
                        throw InvalidSecondaryMajorFilterException("복수전공에는 부필/부선 값을 사용할 수 없습니다.")
                    }
                }
                SecondaryMajorTrackType.MINOR -> {
                    if (completionType == RECOGNIZED) {
                        throw InvalidSecondaryMajorFilterException("부전공은 RECOGNIZED 이수구분을 지원하지 않습니다.")
                    }
                    if (normalized == "복필" || normalized == "복선") {
                        throw InvalidSecondaryMajorFilterException("부전공에는 복필/복선 값을 사용할 수 없습니다.")
                    }
                }
                SecondaryMajorTrackType.CROSS_MAJOR -> {
                    if (completionType != RECOGNIZED) {
                        throw InvalidSecondaryMajorFilterException("타전공인정은 RECOGNIZED 이수구분만 지원합니다.")
                    }
                }
            }
        }
    }
}
