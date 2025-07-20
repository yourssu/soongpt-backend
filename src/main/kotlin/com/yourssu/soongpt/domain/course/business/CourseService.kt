package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.query.GeneralRequiredCourseQuery
import com.yourssu.soongpt.domain.course.business.query.MajorElectiveCourseQuery
import com.yourssu.soongpt.domain.course.business.query.MajorRequiredCourseQuery
import com.yourssu.soongpt.domain.course.business.command.SearchCoursesQuery
import com.yourssu.soongpt.domain.course.business.dto.GeneralRequiredCourseResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorElectiveCourseResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorRequiredCourseResponse
import com.yourssu.soongpt.domain.course.business.dto.SearchCoursesResponse
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeReader
import org.springframework.stereotype.Service

@Service
class CourseService(
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val departmentGradeReader: DepartmentGradeReader,
) {
    fun findAllByDepartmentNameAndGrade(command: MajorRequiredCourseQuery): List<MajorRequiredCourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeInMajorRequired(department, departmentGrade)
        return courses.map { MajorRequiredCourseResponse.from(it) }
    }

    fun findAllByDepartmentNameAndGrade(command: MajorElectiveCourseQuery): List<MajorElectiveCourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeInMajorElective(department, departmentGrade)
        return courses.map { MajorElectiveCourseResponse.from(it) }
    }

    fun findAllByDepartmentNameAndGrade(command: GeneralRequiredCourseQuery): List<GeneralRequiredCourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeInGeneralRequired(department, departmentGrade)
        return courses.map { GeneralRequiredCourseResponse.from(it) }
    }

    fun search(command: SearchCoursesQuery): SearchCoursesResponse {
        val courses = courseReader.search(command.query(), command.toPageable())
        return SearchCoursesResponse.from(courses.content, courses.toPageableInfo())
    }
}
