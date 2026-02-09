package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.MajorCourseRecommendResponse
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

    fun recommend(lowGradeSubjectCodes: List<String>): MajorCourseRecommendResponse {
        if (lowGradeSubjectCodes.isEmpty()) {
            return MajorCourseRecommendResponse.retakeEmpty("재수강 가능한 C+ 이하 과목이 없습니다.")
        }

        val baseCodes = lowGradeSubjectCodes.map { it.toLong() }
        val coursesWithTarget = courseRepository.findCoursesWithTargetByBaseCodes(baseCodes)

        if (coursesWithTarget.isEmpty()) {
            return MajorCourseRecommendResponse.retakeEmpty(
                "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
            )
        }

        val recommendedCourses = coursesWithTarget
            .groupBy { it.course.baseCode() }
            .map { (_, sections) ->
                RecommendedCourseResponse.forRetake(sections)
            }
            .sortedBy { it.courseName }

        return MajorCourseRecommendResponse.retake(recommendedCourses)
    }
}
