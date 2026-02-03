package com.yourssu.soongpt.domain.course.implement

interface FieldListFinder {
    fun getFieldsBySchoolId(schoolId: Int): List<String>
    fun getAllFieldsGrouped(): Map<Int, List<String>>
}
