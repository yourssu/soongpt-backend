package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.GeneralRequiredResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorElectiveResponse
import com.yourssu.soongpt.domain.course.business.dto.MajorRequiredResponse
import com.yourssu.soongpt.domain.course.business.query.GeneralRequiredCourseQuery
import com.yourssu.soongpt.domain.course.business.query.MajorElectiveCourseQuery
import com.yourssu.soongpt.domain.course.business.query.MajorRequiredCourseQuery
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
        val target = targetReader.getByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllByTargetInMajorRequired(target)
        return courses.map { MajorRequiredResponse.from(it) }
    }

    fun findAll(query: MajorElectiveCourseQuery): List<MajorElectiveResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val target = targetReader.getByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllByTargetInMajorElective(target)
        return courses.map { MajorElectiveResponse.from(it) }
    }

    fun findAll(query: GeneralRequiredCourseQuery): List<GeneralRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val target = targetReader.getByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllByTargetInGeneralRequired(target)
        return courses.map { GeneralRequiredResponse.from(it) }
    }
}
