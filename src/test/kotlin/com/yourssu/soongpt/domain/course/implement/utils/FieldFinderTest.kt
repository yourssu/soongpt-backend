package com.yourssu.soongpt.domain.course.implement.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FieldFinderTest {

    @Test
    fun `prefers higher start year when multiple after entries match`() {
        val field = """
            ['22이후] OLD
            ['23이후] NEW
        """.trimIndent()

        assertEquals("NEW", FieldFinder.findFieldBySchoolId(field, 23))
        assertEquals("NEW", FieldFinder.findFieldBySchoolId(field, 24))
        assertEquals("OLD", FieldFinder.findFieldBySchoolId(field, 22))
    }

    @Test
    fun `parses after even with spaces and list prefix`() {
        val field = """
            - ['22 이후] OLD
            - ['23 이후] NEW
        """.trimIndent()

        assertEquals("NEW", FieldFinder.findFieldBySchoolId(field, 23))
    }

    @Test
    fun `normalizes parenthesized field name to inner label`() {
        val field = """
            ['23이후] 품격(글로벌시민의식)
        """.trimIndent()

        assertEquals("글로벌시민의식", FieldFinder.findFieldBySchoolId(field, 23))
    }
}
