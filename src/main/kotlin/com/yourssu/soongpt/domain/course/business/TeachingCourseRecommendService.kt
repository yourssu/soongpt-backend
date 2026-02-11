package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.application.RecommendContext
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toTakenBaseCodeSet
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service

@Service
class TeachingCourseRecommendService(
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
    private val courseReader: CourseReader,
) {

    fun recommend(ctx: RecommendContext): CategoryRecommendResponse {
        if (!ctx.flags.teaching) {
            return messageResponse("교직이수 대상이 아닙니다.")
        }

        val department = departmentReader.getByName(ctx.departmentName)

        val allCourseCodes = (1..5).flatMap { grade ->
            targetReader.findAllByDepartmentGrade(department, grade) +
                targetReader.findAllByDepartmentGradeForTeaching(department, grade)
        }.distinct()

        val courses = courseReader.findAllInCategory(Category.TEACHING, allCourseCodes, ctx.schoolId)

        val takenBaseCodes = toTakenBaseCodeSet(ctx.takenSubjectCodes)
        val untaken = courses.filter { it.baseCode() !in takenBaseCodes }

        if (untaken.isEmpty()) {
            return messageResponse("이번 학기에 수강 가능한 교직 과목이 없습니다.")
        }

        val areaOrder = TeachingMajorArea.entries.associateWith { it.ordinal }

        val recommendedCourses = untaken
            .groupBy { course ->
                TeachingMajorArea.entries.firstOrNull { area ->
                    area.fieldValues.any { fieldValue ->
                        course.field?.contains(fieldValue) == true
                    }
                }
            }
            .flatMap { (area, coursesInArea) ->
                coursesInArea
                    .groupBy { it.baseCode() }
                    .map { (_, sections) ->
                        RecommendedCourseResponse.forTeaching(sections, area)
                    }
                    .sortedBy { it.courseName }
                    .map { it to (areaOrder[area] ?: Int.MAX_VALUE) }
            }
            .sortedWith(compareBy({ it.second }, { it.first.courseName }))
            .map { it.first }

        return CategoryRecommendResponse(
            category = TEACHING_CATEGORY,
            progress = null,
            message = null,
            userGrade = null,
            courses = recommendedCourses,
            lateFields = null,
        )
    }

    private fun messageResponse(message: String) = CategoryRecommendResponse(
        category = TEACHING_CATEGORY,
        progress = null,
        message = message,
        userGrade = null,
        courses = emptyList(),
        lateFields = null,
    )

    companion object {
        private const val TEACHING_CATEGORY = "TEACHING"
    }
}
