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

        val grouped = untakenCourses.groupBy { it.course.baseCode() }
        val recommendedCourses = buildRecommendedCourses(grouped, userGrade)
        val gradeGroups = if (completionType == SecondaryMajorCompletionType.ELECTIVE) {
            buildGradeGroups(grouped, userGrade)
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
        grouped: Map<Long, List<CourseWithTarget>>,
        userGrade: Int,
    ): List<RecommendedCourseResponse> {
        return grouped.entries
            .sortedWith(
                compareBy(
                    { if (it.value.first().isLateFor(userGrade)) 0 else 1 },
                    { it.value.first().targetGrades.maxOrNull() ?: 1 },
                    { it.value.first().course.name },
                )
            )
            .map { (_, sections) ->
                val representative = sections.first()
                RecommendedCourseResponse.from(
                    coursesWithTarget = sections,
                    isLate = representative.isLateFor(userGrade),
                )
            }
    }

    private fun buildGradeGroups(
        grouped: Map<Long, List<CourseWithTarget>>,
        userGrade: Int,
    ): List<GradeGroupResponse> {
        return grouped.entries
            .groupBy { it.value.first().targetGrades.maxOrNull() ?: 1 }
            .entries
            .sortedBy { it.key }
            .map { (grade, entries) ->
                val courses = entries.map { (_, sections) ->
                    val representative = sections.first()
                    RecommendedCourseResponse.from(
                        coursesWithTarget = sections,
                        isLate = representative.isLateFor(userGrade),
                    )
                }
                GradeGroupResponse(grade = grade, courses = courses)
            }
    }

    companion object {
        private const val MAX_GRADE = 5
    }
}
