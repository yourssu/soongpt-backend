package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.FieldNullPointException
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CourseReader(
    private val courseRepository: CourseRepository,
    private val targetRepository: TargetRepository,
    private val fieldListFinder: FieldListFinder,
) {
    fun findAllByClass(department: Department, code: Long, grade: Int): List<Course> {
        val isBaseCode = code.toString().length <= 8
        if (isBaseCode) {
            val courses = courseRepository.findAllByClass(code)
            if (courses.isEmpty()) return emptyList()
            if (courses.first().category == Category.GENERAL_REQUIRED) {
                val targets = targetRepository.findAllByClass(department.id!!, code, grade)
                return courseRepository.findAllById(targets.map { it.courseCode })
            }
            return courses
        }

        val course = courseRepository.get(code)
        if (course.category == Category.GENERAL_REQUIRED) {
            val targets = targetRepository.findAllByClass(department.id!!, code, grade)
            return courseRepository.findAllById(targets.map { it.courseCode })
        }
        return courseRepository.findAllByClass(code)
    }

    fun findAllBy(category: Category, department: Department, grade: Int): List<Course> {
        val departmentId = department.id ?: return emptyList()
        val courseCodes = targetRepository.findAllByDepartmentGrade(departmentId, department.collegeId, grade)
        return courseRepository.findAllInCategory(category, courseCodes)
    }

    fun findAllInCategory(category: Category?, courseCodes: List<Long>, schoolId: Int): List<Course> {
        if (category == null) {
            return findAllByCodes(courseCodes, schoolId)
        }
        val courses = courseRepository.findAllInCategory(category, courseCodes)

        // 교직 과목 field는 "전공영역/교과교육영역"처럼 이미 최종값이므로 FieldFinder(학번 파싱) 적용 대상이 아님
        if (category == Category.TEACHING) {
            return courses
        }

        return courses.map { it -> it.copy(field = FieldFinder.findFieldBySchoolId(it.field?: throw FieldNullPointException(), schoolId)) }
    }

    private fun findAllByCodes(courseCodes: List<Long>, schoolId: Int): List<Course> {
        val courses = courseRepository.findAllByCode(courseCodes)
        return courses.map { it -> it.copy(field = FieldFinder.findFieldBySchoolId(it.field?: throw FieldNullPointException(), schoolId)) }
    }

    fun findAllInCategory(category: Category?, courseCodes: List<Long>, field: String, schoolId: Int): List<Course> {
        if (category == null) {
            return findAllByCodesAndField(courseCodes, field, schoolId)
        }
        val courses = courseRepository.findAllInCategory(category, courseCodes)
        return courses.map { it -> it.copy(field = FieldFinder.findFieldBySchoolId(field, schoolId)) }
            .filter { it.field?.contains(field) == true }
    }

    fun findAllByCodesAndField(courseCodes: List<Long>, field: String, schoolId: Int): List<Course> {
        val courses = courseRepository.findAllByCode(courseCodes)
        return courses.map { it -> it.copy(field = FieldFinder.findFieldBySchoolId(field, schoolId)) }
            .filter { it.field?.contains(field) == true }
    }

    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        return courseRepository.groupByCategory(codes)
    }

    fun searchCourses(query: String, pageable: Pageable): Page<Course> {
        if (query.isBlank()) {
            return courseRepository.findAll(pageable)
        }
        return courseRepository.searchCourses(query, pageable)
    }

    fun findAllByCode(codes: List<Long>): List<Course> {
        return courseRepository.findAllByCode(codes)
    }

    fun getFieldsBySchoolId(schoolId: Int): List<String> {
        return fieldListFinder.getFieldsBySchoolId(schoolId)
    }

    fun getAllFieldsGrouped(): Map<Int, List<String>> {
        return fieldListFinder.getAllFieldsGrouped()
    }

    fun findByCode(code: Long): Course {
        return courseRepository.get(code)
    }

    fun save(course: Course): Course {
        return courseRepository.save(course)
    }

    fun delete(code: Long) {
        courseRepository.delete(code)
    }

    fun findCoursesWithTargetBySecondaryMajor(
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        departmentId: Long,
        collegeId: Long,
        maxGrade: Int,
    ): List<CourseWithTarget> {
        return courseRepository.findCoursesWithTargetBySecondaryMajor(
            trackType = trackType,
            completionType = completionType,
            departmentId = departmentId,
            collegeId = collegeId,
            maxGrade = maxGrade,
        )
    }
}
