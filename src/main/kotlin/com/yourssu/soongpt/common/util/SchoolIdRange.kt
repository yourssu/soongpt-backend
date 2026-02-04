package com.yourssu.soongpt.common.util

import java.time.LocalDate

object SchoolIdRange {
    private val currentYear = LocalDate.now().year

    val MIN_SCHOOL_ID = (currentYear - 10) % 100
    val MAX_SCHOOL_ID = currentYear % 100

    fun isValid(schoolId: Int): Boolean {
        return schoolId in MIN_SCHOOL_ID..MAX_SCHOOL_ID
    }

    fun getValidationMessage(): String {
        return "학번은 ${MIN_SCHOOL_ID}부터 ${MAX_SCHOOL_ID}까지 가능합니다."
    }

    fun getRange(): IntRange {
        return MIN_SCHOOL_ID..MAX_SCHOOL_ID
    }
}
