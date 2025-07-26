package com.yourssu.soongpt.domain.timetable.implement.dto

data class CourseCandidates(
    val candidates: List<CourseCandidate>
) {
    companion object {
        fun from(candidates: List<CourseCandidate>): CourseCandidates {
            return CourseCandidates(
                candidates = candidates
            )
        }
    }
}
