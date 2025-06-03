package com.yourssu.soongpt.domain.course.storage

import CourseMapper
import com.yourssu.soongpt.common.infrastructure.FileReader
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course2
import com.yourssu.soongpt.domain.course.implement.JsonCourseRepository
import com.yourssu.soongpt.domain.course.implement.dto.SearchCourseDto
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class JsonCourseRepositoryImpl(
) : JsonCourseRepository {
    override fun findAllByDepartmentAndGrade(
        department: Department,
        departmentGrade: DepartmentGrade,
        category: Category
    ): List<Course2> {
        val year = 2025
        val semester = 1
        val file = FileReader.getFile(
            "/result/${year}_${semester}/",
            listOf(department.name, "${departmentGrade.grade}_${category.displayName}"),
        )
        if (category == Category.MAJOR_REQUIRED) {
            val file2 = FileReader.getFile(
                "/result/${year}_${semester}/",
                listOf(department.name, "${departmentGrade.grade}_전기"),
            )
            return CourseMapper.toCourseDomain(file.readText()) +
                CourseMapper.toCourseDomain(file2.readText())
        }
        return CourseMapper.toCourseDomain(file.readText())
    }

    override fun search(
        query: String,
        pageable: Pageable
    ): SearchCourseDto {
        val year = 2025
        val semester = 1
        val file = FileReader.getFile(
            "/result/${year}_${semester}/",
                listOf("*")
            )
        val courses = CourseMapper.toCourseDomain(file.readText())
        return CourseFinder.search(courses, query, pageable)
    }
}
