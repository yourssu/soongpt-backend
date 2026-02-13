package com.yourssu.soongpt.domain.course.application.dto

import com.yourssu.soongpt.common.handler.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

class GetTeachingCoursesRequestTest : BehaviorSpec({

    given("GetTeachingCoursesRequest.toQuery") {
        `when`("majorArea가 유효하지 않으면") {
            then("BadRequestException을 던진다") {
                val request = GetTeachingCoursesRequest(
                    schoolId = 26,
                    department = "컴퓨터학부",
                    majorArea = "INVALID",
                )

                val exception = shouldThrow<BadRequestException> {
                    request.toQuery()
                }

                exception.message shouldContain "majorArea"
            }
        }
    }
})
