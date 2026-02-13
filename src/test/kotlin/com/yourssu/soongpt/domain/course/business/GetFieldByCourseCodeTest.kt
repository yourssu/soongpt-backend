package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.coursefield.implement.CourseField
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetFieldByCourseCodeTest : BehaviorSpec({

    given("getFieldByCourseCode") {
        `when`("분반 코드(10자리)로 요청되고, course_field가 baseCode(8자리)에만 존재하면") {
            then("baseCode로 fallback하여 field를 반환한다") {
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

                service.getFieldByCourseCode(sectionCode, 23) shouldBe "NEW"
            }
        }
    }
})
