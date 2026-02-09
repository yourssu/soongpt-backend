package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.FieldGroupResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import org.springframework.stereotype.Service

/**
 * 교양 과목 추천 서비스
 * - 교양필수(GENERAL_REQUIRED), 교양선택(GENERAL_ELECTIVE) 추천
 * - 분야(field)별 그룹핑
 * - 교필: LATE 분야는 텍스트만, ON_TIME 분야는 과목 포함
 * - 교선: 분야별 과목 그룹핑
 */
@Service
class GeneralCourseRecommendService(
    private val courseRepository: CourseRepository,
    private val departmentReader: DepartmentReader,
) {

    fun recommend(
        category: Category,
        departmentName: String,
        userGrade: Int,
        schoolId: Int,
        takenSubjectCodes: List<String>,
        progress: Progress,
    ): CategoryRecommendResponse {
        require(category == Category.GENERAL_REQUIRED || category == Category.GENERAL_ELECTIVE) {
            "Category must be GENERAL_REQUIRED or GENERAL_ELECTIVE"
        }

        if (progress.satisfied) {
            return satisfied(category, progress)
        }

        val department = departmentReader.getByName(departmentName)
        val allCourses = courseRepository.findCoursesWithTargetByCategory(
            category = category,
            departmentId = department.id!!,
            collegeId = department.collegeId,
            maxGrade = userGrade,
        ).distinctBy { it.course.code }

        if (allCourses.isEmpty()) {
            return empty(category, progress)
        }

        val takenBaseCodes = takenSubjectCodes.mapNotNull { it.toLongOrNull() }.toSet()

        // 과목별 field 파싱 후 (field → courses) 그룹핑
        val coursesByField = allCourses
            .mapNotNull { cwt ->
                val fieldName = cwt.course.field
                    ?.let { FieldFinder.findFieldBySchoolId(it, schoolId) }
                    ?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                Pair(fieldName, cwt)
            }
            .groupBy({ it.first }, { it.second })

        // 분야 단위 이수 필터링: 해당 분야에 이미 수강한 과목이 하나라도 있으면 분야 전체 제외
        val untakenFields = coursesByField.filter { (_, courses) ->
            courses.none { it.course.baseCode() in takenBaseCodes }
        }

        if (untakenFields.isEmpty()) {
            return empty(category, progress)
        }

        return when (category) {
            Category.GENERAL_REQUIRED -> buildGeneralRequiredResponse(
                untakenFields, userGrade, progress,
            )
            else -> buildGeneralElectiveResponse(
                untakenFields, userGrade, category, progress,
            )
        }
    }

    /**
     * 교양필수: LATE 분야 → lateFields(텍스트), ON_TIME 분야 → fieldGroups(과목 포함)
     */
    private fun buildGeneralRequiredResponse(
        untakenFields: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        progress: Progress,
    ): CategoryRecommendResponse {
        val lateFields = mutableListOf<String>()
        val onTimeFieldGroups = mutableListOf<FieldGroupResponse>()

        for ((fieldName, courses) in untakenFields) {
            val isFieldLate = courses.all { it.isLateFor(userGrade) }
            if (isFieldLate) {
                lateFields.add(fieldName)
            } else {
                onTimeFieldGroups.add(buildFieldGroup(fieldName, courses, userGrade))
            }
        }

        return CategoryRecommendResponse(
            category = Category.GENERAL_REQUIRED.name,
            progress = progress,
            message = null,
            userGrade = null,
            courses = emptyList(),
            gradeGroups = null,
            fieldGroups = onTimeFieldGroups.ifEmpty { null },
            lateFields = lateFields.ifEmpty { null },
        )
    }

    /**
     * 교양선택: 분야별 과목 그룹핑 (LATE/ON_TIME 구분 없이)
     */
    private fun buildGeneralElectiveResponse(
        untakenFields: Map<String, List<CourseWithTarget>>,
        userGrade: Int,
        category: Category,
        progress: Progress,
    ): CategoryRecommendResponse {
        val fieldGroups = untakenFields.entries
            .map { (fieldName, courses) -> buildFieldGroup(fieldName, courses, userGrade) }

        return CategoryRecommendResponse(
            category = category.name,
            progress = progress,
            message = null,
            userGrade = null,
            courses = emptyList(),
            gradeGroups = null,
            fieldGroups = fieldGroups.ifEmpty { null },
            lateFields = null,
        )
    }

    private fun buildFieldGroup(
        fieldName: String,
        courses: List<CourseWithTarget>,
        userGrade: Int,
    ): FieldGroupResponse {
        val grouped = courses.groupBy { it.course.baseCode() }
        val recommended = grouped.entries
            .sortedBy { it.value.first().course.name }
            .map { (_, sections) ->
                val representative = sections.first()
                RecommendedCourseResponse.from(
                    coursesWithTarget = sections,
                    isLate = representative.isLateFor(userGrade),
                )
            }
        return FieldGroupResponse(field = fieldName, courses = recommended)
    }

    private fun satisfied(category: Category, progress: Progress): CategoryRecommendResponse {
        val message = when (category) {
            Category.GENERAL_REQUIRED -> "교양필수 학점을 이미 모두 이수하셨습니다."
            Category.GENERAL_ELECTIVE -> "교양선택 학점을 이미 모두 이수하셨습니다."
            else -> "이미 모두 이수하셨습니다."
        }
        return CategoryRecommendResponse(
            category = category.name,
            progress = progress,
            message = message,
            userGrade = null,
            courses = emptyList(),
            gradeGroups = null,
            fieldGroups = null,
            lateFields = null,
        )
    }

    private fun empty(category: Category, progress: Progress): CategoryRecommendResponse {
        val message = when (category) {
            Category.GENERAL_REQUIRED -> "이번 학기에 수강 가능한 교양필수 과목이 없습니다."
            Category.GENERAL_ELECTIVE -> "이번 학기에 수강 가능한 교양선택 과목이 없습니다."
            else -> "이번 학기에 수강 가능한 과목이 없습니다."
        }
        return CategoryRecommendResponse(
            category = category.name,
            progress = progress,
            message = message,
            userGrade = null,
            courses = emptyList(),
            gradeGroups = null,
            fieldGroups = null,
            lateFields = null,
        )
    }
}
