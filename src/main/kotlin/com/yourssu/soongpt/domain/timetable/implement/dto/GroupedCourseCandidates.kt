package com.yourssu.soongpt.domain.timetable.implement.dto

import com.yourssu.soongpt.domain.course.implement.Course

data class GroupedCourseCandidates(
        val retake: List<List<Course>>,
        val majorRequired: List<List<Course>>,
        val majorElective: List<List<Course>>,
        val otherMajor: List<List<Course>>,
        val generalRequired: List<List<Course>>,
        val added: List<List<Course>>
) {
    fun getAllOrdered(): List<List<Course>> {
        return retake + majorRequired + majorElective + otherMajor + generalRequired + added
    }
}
