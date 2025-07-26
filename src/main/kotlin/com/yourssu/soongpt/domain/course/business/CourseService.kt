package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.GeneralRequiredResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorElectiveResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorRequiredResponse
import com.yourssu.soongpt.domain.course.business.dto.SearchCoursesResponse
import com.yourssu.soongpt.domain.course.business.query.GeneralRequiredCourseQuery
import com.yourssu.soongpt.domain.course.business.query.MajorElectiveCourseQuery
import com.yourssu.soongpt.domain.course.business.query.MajorRequiredCourseQuery
import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
) {
    fun findAll(query: MajorRequiredCourseQuery): List<MajorRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val targets = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllInCategory(Category.MAJOR_REQUIRED, targets.map { it.courseId })
        return courses.map { MajorRequiredResponse.from(it) }
    }

    fun findAll(query: MajorElectiveCourseQuery): List<MajorElectiveResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val targets = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllInCategory(Category.MAJOR_ELECTIVE, targets.map { it.courseId })
        return courses.map { MajorElectiveResponse.from(it) }
    }

    fun findAll(query: GeneralRequiredCourseQuery): List<GeneralRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val targets = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllInCategory(Category.GENERAL_REQUIRED, targets.map { it.courseId })
        return courses.map { GeneralRequiredResponse.from(it) }
    }

    fun search(query: SearchCoursesQuery): SearchCoursesResponse {
        val page = courseReader.searchCourses(query.query, query.toPageable())
        return SearchCoursesResponse.from(page)
    }
}
