package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.handler.BadRequestException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class GetTeachingCoursesRequestTest {

    @Test
    fun `invalid majorArea throws BadRequestException`() {
        val request = GetTeachingCoursesRequest(
            schoolId = 26,
            department = "컴퓨터학부",
            majorArea = "INVALID",
        )

        assertThrows(BadRequestException::class.java) {
            request.toQuery()
        }
    }

    @Test
    fun `department alias IT융합전공 is normalized`() {
        val request = GetTeachingCoursesRequest(
            schoolId = 26,
            department = "IT융합전공",
            majorArea = "전공영역",
        )

        val query = request.toQuery()
        kotlin.test.assertEquals("전자정보공학부 IT융합전공", query.departmentName)
    }
}
