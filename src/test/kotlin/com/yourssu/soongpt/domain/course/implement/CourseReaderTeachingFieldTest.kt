package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.target.implement.TargetRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CourseReaderTeachingFieldTest : BehaviorSpec({

    given("CourseReader.findAllInCategory") {
        `when`("category가 TEACHING이면") {
            then("FieldFinder 변환 없이 course.field 원문을 그대로 유지한다") {
                val courseRepository = mock<CourseRepository>()
                val targetRepository = mock<TargetRepository>()
                val fieldListFinder = mock<FieldListFinder>()

                val course = Course(
                    id = 1L,
                    category = Category.TEACHING,
                    subCategory = null,
                    multiMajorCategory = null,
                    field = "교직영역/교직이론",
                    code = 2150118601,
                    name = "교육학개론",
                    professor = null,
                    department = "학사팀",
                    division = null,
                    time = "2.0",
                    point = "2.0",
                    personeel = 0,
                    scheduleRoom = "",
                    target = "",
                    credit = 2.0,
                )

                whenever(courseRepository.findAllInCategory(Category.TEACHING, listOf(course.code))).thenReturn(listOf(course))

                val reader = CourseReader(courseRepository, targetRepository, fieldListFinder)

                val result = reader.findAllInCategory(Category.TEACHING, listOf(course.code), 26)

                result shouldHaveSize 1
                result.first().field shouldBe "교직영역/교직이론"
            }
        }
    }
})
