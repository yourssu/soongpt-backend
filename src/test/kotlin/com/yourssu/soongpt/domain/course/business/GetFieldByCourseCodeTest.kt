package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.coursefield.implement.CourseField
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetFieldByCourseCodeTest {

    @Test
    fun `finds courseField by baseCode when section code is provided`() {
        val courseReader = mock<CourseReader>()
        val departmentReader = mock<DepartmentReader>()
        val targetReader = mock<TargetReader>()
        val collegeReader = mock<CollegeReader>()
        val courseFieldReader = mock<CourseFieldReader>()

        val baseCode = 21500118L
        val sectionCode = 2150011801L

        whenever(courseFieldReader.findByCourseCode(sectionCode)).thenReturn(null)
        whenever(courseFieldReader.findByCourseCode(baseCode)).thenReturn(
            CourseField(
                id = 1L,
                courseCode = baseCode,
                courseName = "dummy",
                field = "['22이후]OLD\n['23이후]NEW",
            )
        )

        val service = CourseServiceImpl(
            courseReader = courseReader,
            departmentReader = departmentReader,
            targetReader = targetReader,
            collegeReader = collegeReader,
            courseFieldReader = courseFieldReader,
        )

        assertEquals("NEW", service.getFieldByCourseCode(sectionCode, 23))
    }
}
