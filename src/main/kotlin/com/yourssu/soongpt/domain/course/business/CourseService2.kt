package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.command.MajorRequiredCourseFoundCommand
import com.yourssu.soongpt.domain.course.business.dto.MajorRequiredCourseResponse
import com.yourssu.soongpt.domain.course.implement.CourseReader2
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeReader
import org.springframework.stereotype.Service

@Service
class CourseService2(
    private val courseReader: CourseReader2,
    private val departmentReader: DepartmentReader,
    private val departmentGradeReader: DepartmentGradeReader,
) {
    fun findAllByDepartmentNameAndGrade(command: MajorRequiredCourseFoundCommand): List<MajorRequiredCourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        return courseReader.findAllByDepartmentGradeInMajorRequired(department, departmentGrade).map { MajorRequiredCourseResponse.from(it) }
    }
}
