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
}
