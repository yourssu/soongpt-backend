package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.baseCode
import org.springframework.stereotype.Service

/**
 * 재수강 과목 추천 서비스
 * - C 이하 성적의 과목 중 이번 학기 개설 과목을 추천
 */
@Service
class RetakeCourseRecommendService(
    private val courseRepository: CourseRepository,
) {

    fun recommend(lowGradeSubjectCodes: List<String>): CategoryRecommendResponse {
        if (lowGradeSubjectCodes.isEmpty()) {
            return retakeEmpty("재수강 가능한 C+ 이하 과목이 없습니다.")
        }

        val baseCodes = lowGradeSubjectCodes.mapNotNull { it.toLongOrNull() }
        if (baseCodes.isEmpty()) {
            return retakeEmpty("재수강 가능한 C+ 이하 과목이 없습니다.")
        }
        val coursesWithTarget = courseRepository.findCoursesWithTargetByBaseCodes(baseCodes)

        if (coursesWithTarget.isEmpty()) {
            return retakeEmpty(
                "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
            )
        }

        val recommendedCourses = coursesWithTarget
            .groupBy { it.course.baseCode() }
            .map { (_, sections) ->
                RecommendedCourseResponse.forRetake(sections)
            }
            .sortedBy { it.courseName }

        return CategoryRecommendResponse(
            category = RETAKE_CATEGORY,
            progress = null,
            message = null,
            userGrade = null,
            courses = recommendedCourses,
            lateFields = null,
        )
    }

    private fun retakeEmpty(message: String) = CategoryRecommendResponse(
        category = RETAKE_CATEGORY,
        progress = null,
        message = message,
        userGrade = null,
        courses = emptyList(),
        lateFields = null,
    )

    companion object {
        private const val RETAKE_CATEGORY = "RETAKE"
    }
}
