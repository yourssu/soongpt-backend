package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.GradeGroupResponse
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.business.dto.SecondaryMajorCourseRecommendResponse
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import org.springframework.stereotype.Service

@Service
class SecondaryMajorCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
) {
    fun recommend(
        departmentName: String,
        userGrade: Int,
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        takenSubjectCodes: List<String>,
        progress: String? = null,
        satisfied: Boolean = false,
    ): SecondaryMajorCourseRecommendResponse {
        if (satisfied) {
            return SecondaryMajorCourseRecommendResponse.satisfied(
                trackType = trackType,
                completionType = completionType,
                progress = progress,
            )
        }

        val department = departmentReader.getByName(departmentName)
        val maxGrade = when (completionType) {
            SecondaryMajorCompletionType.REQUIRED -> userGrade
            SecondaryMajorCompletionType.ELECTIVE,
            SecondaryMajorCompletionType.RECOGNIZED,
            -> MAX_GRADE
        }

        val untakenCourses = getUntakenCoursesWithTarget(
            trackType = trackType,
            completionType = completionType,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = maxGrade,
            takenSubjectCodes = takenSubjectCodes,
        )

        if (untakenCourses.isEmpty()) {
            return SecondaryMajorCourseRecommendResponse.empty(
                trackType = trackType,
                completionType = completionType,
                progress = progress,
            )
        }

        val recommendedCourses = buildRecommendedCourses(untakenCourses, userGrade)
        val gradeGroups = if (completionType == SecondaryMajorCompletionType.ELECTIVE) {
            buildGradeGroups(recommendedCourses)
        } else {
            null
        }

        return SecondaryMajorCourseRecommendResponse.of(
            trackType = trackType,
            completionType = completionType,
            progress = progress,
            satisfied = false,
            courses = recommendedCourses,
            gradeGroups = gradeGroups,
        )
    }

    private fun getUntakenCoursesWithTarget(
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        departmentId: Long,
        collegeId: Long,
        maxGrade: Int,
        takenSubjectCodes: List<String>,
    ): List<CourseWithTarget> {
        val coursesWithTarget = courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = trackType,
            completionType = completionType,
            departmentId = departmentId,
            collegeId = collegeId,
            maxGrade = maxGrade,
        )
        if (coursesWithTarget.isEmpty()) {
            return emptyList()
        }

        val takenBaseCodes = takenSubjectCodes.mapNotNull { it.toLongOrNull() }.toSet()
        return coursesWithTarget
            .distinctBy { it.course.code }
            .filter { it.course.baseCode() !in takenBaseCodes }
    }

    private fun buildRecommendedCourses(
        coursesWithTarget: List<CourseWithTarget>,
        userGrade: Int,
    ): List<RecommendedCourseResponse> {
        return coursesWithTarget
            .groupBy { it.course.baseCode() }
            .map { (_, sections) ->
                val representative = sections.first()
                RecommendedCourseResponse.from(
                    courses = sections.map { it.course },
                    targetGrades = representative.targetGrades,
                    isLate = representative.isLateFor(userGrade),
                )
            }
            .sortedWith(
                compareBy(
                    { it.timing.ordinal },
                    { it.targetGrades.maxOrNull() ?: 1 },
                    { it.courseName },
                )
            )
    }

    private fun buildGradeGroups(courses: List<RecommendedCourseResponse>): List<GradeGroupResponse> {
        return courses
            .groupBy { it.targetGrades.maxOrNull() ?: 1 }
            .map { (grade, groupCourses) ->
                GradeGroupResponse(grade = grade, courses = groupCourses)
            }
            .sortedBy { it.grade }
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
