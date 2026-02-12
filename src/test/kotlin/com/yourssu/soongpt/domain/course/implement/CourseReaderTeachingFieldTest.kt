package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class CourseReaderTeachingFieldTest {

    @Test
    fun `teaching category keeps original field without FieldFinder transformation`() {
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

        assertEquals(1, result.size)
        assertEquals("교직영역/교직이론", result.first().field)
    }
}
