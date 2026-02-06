package com.yourssu.soongpt.domain.timetable.implement

import org.springframework.stereotype.Component

@Component
class TakenCourseChecker {
    // TODO: piki가 제공할 기수강 과목 조회 로직 (mock)
    fun isCourseTaken(userId: String, courseCode: Long): Boolean {
        // 임시로 특정 userId와 courseCode에 대해 true 반환
        // 실제 구현에서는 외부 서비스 호출 또는 DB 조회
        return (userId == "testUser" && courseCode == 12345L)
    }
}
