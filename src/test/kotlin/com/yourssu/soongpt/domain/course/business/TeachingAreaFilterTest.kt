package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.course.business.query.FilterTeachingCoursesQuery
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.TeachingArea
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class TeachingAreaFilterTest {

    @Test
    fun `teachingArea filters by keywords`() {
        val departmentReader = mock<DepartmentReader>()
        val targetReader = mock<TargetReader>()
        val courseReader = mock<CourseReader>()
        val collegeReader = mock<CollegeReader>()
        val courseFieldReader = mock<CourseFieldReader>()

        val department = Department(id = 1L, name = "컴퓨터학부", collegeId = 1L)
        whenever(departmentReader.getByName("컴퓨터학부")).thenReturn(department)

        // grade 1..5 -> return a fixed set
        (1..5).forEach { grade ->
            whenever(targetReader.findAllByDepartmentGrade(department, grade)).thenReturn(listOf(1L, 2L))
        }

        val courses = listOf(
            Course(
                id = 1L,
                category = Category.TEACHING,
                subCategory = null,
                multiMajorCategory = null,
                field = "교직이론영역",
                code = 1L,
                name = "교육학개론",
                professor = null,
                department = "학사팀",
                division = null,
                time = "",
                point = "",
                personeel = 0,
                scheduleRoom = "",
                target = "",
                credit = null,
            ),
            Course(
                id = 2L,
                category = Category.TEACHING,
                subCategory = null,
                multiMajorCategory = null,
                field = "교직이론영역",
                code = 2L,
                name = "교직실무",
                professor = null,
                department = "학사팀",
                division = null,
                time = "",
                point = "",
                personeel = 0,
                scheduleRoom = "",
                target = "",
                credit = null,
            ),
        )

        whenever(courseReader.findAllInCategory(Category.TEACHING, listOf(1L, 2L), 26)).thenReturn(courses)

        val service = CourseServiceImpl(
            courseReader = courseReader,
            departmentReader = departmentReader,
            targetReader = targetReader,
            collegeReader = collegeReader,
            courseFieldReader = courseFieldReader,
        )

        val result = service.findAllTeachingCourses(
            FilterTeachingCoursesQuery(
                schoolId = 26,
                departmentName = "컴퓨터학부",
                teachingArea = TeachingArea.LITERACY,
            )
        )

        assertEquals(1, result.size)
        assertEquals(2L, result.first().code)
    }
}
