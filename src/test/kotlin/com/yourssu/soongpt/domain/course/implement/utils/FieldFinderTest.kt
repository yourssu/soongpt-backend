package com.yourssu.soongpt.domain.course.implement.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FieldFinderTest {

    @Test
    fun `prefers higher start year when multiple 'after' entries match`() {
        val field = """
            ['22이후] A
            ['23이후] B
        """.trimIndent()

        assertEquals("B", FieldFinder.findFieldBySchoolId(field, 23))
        assertEquals("B", FieldFinder.findFieldBySchoolId(field, 24))
        assertEquals("A", FieldFinder.findFieldBySchoolId(field, 22))
    }

    @Test
    fun `parses 'after' even with spaces`() {
        val field = """
            - ['22 이후] A
            - ['23 이후] B
        """.trimIndent()

        assertEquals("B", FieldFinder.findFieldBySchoolId(field, 23))
    }
}
