package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType

data class SecondaryMajorCourseRecommendResponse(
    val trackType: String,
    val completionType: String,
    val classification: String,
    val progress: String?,
    val satisfied: Boolean,
    val courses: List<RecommendedCourseResponse>,
    val gradeGroups: List<GradeGroupResponse>? = null,
    val message: String? = null,
) {
    companion object {
        fun of(
            trackType: SecondaryMajorTrackType,
            completionType: SecondaryMajorCompletionType,
            progress: String?,
            satisfied: Boolean,
            courses: List<RecommendedCourseResponse>,
            gradeGroups: List<GradeGroupResponse>? = null,
            message: String? = null,
        ): SecondaryMajorCourseRecommendResponse {
            return SecondaryMajorCourseRecommendResponse(
                trackType = trackType.displayName,
                completionType = completionType.displayName,
                classification = trackType.classificationLabel(completionType),
                progress = progress,
                satisfied = satisfied,
                courses = courses,
                gradeGroups = gradeGroups,
                message = message,
            )
        }

        fun satisfied(
            trackType: SecondaryMajorTrackType,
            completionType: SecondaryMajorCompletionType,
            progress: String?,
        ): SecondaryMajorCourseRecommendResponse {
            val message = when (trackType.classificationLabel(completionType)) {
                "복필" -> "복수전공 필수 학점을 이미 모두 이수하셨습니다."
                "복선" -> "복수전공 선택 학점을 이미 모두 이수하셨습니다."
                "부필" -> "부전공 필수 학점을 이미 모두 이수하셨습니다."
                "부선" -> "부전공 선택 학점을 이미 모두 이수하셨습니다."
                else -> "타전공인정 과목 요건을 이미 충족하셨습니다."
            }
            return of(
                trackType = trackType,
                completionType = completionType,
                progress = progress,
                satisfied = true,
                courses = emptyList(),
                message = message,
            )
        }

        fun empty(
            trackType: SecondaryMajorTrackType,
            completionType: SecondaryMajorCompletionType,
            progress: String?,
        ): SecondaryMajorCourseRecommendResponse {
            val message = when (trackType.classificationLabel(completionType)) {
                "복필" -> "이번 학기에 수강 가능한 복수전공 필수 과목이 없습니다."
                "복선" -> "이번 학기에 수강 가능한 복수전공 선택 과목이 없습니다."
                "부필" -> "이번 학기에 수강 가능한 부전공 필수 과목이 없습니다."
                "부선" -> "이번 학기에 수강 가능한 부전공 선택 과목이 없습니다."
                else -> "이번 학기에 수강 가능한 타전공인정 과목이 없습니다."
            }
            return of(
                trackType = trackType,
                completionType = completionType,
                progress = progress,
                satisfied = false,
                courses = emptyList(),
                message = message,
            )
        }
    }
}
