package com.yourssu.soongpt.domain.course.implement

/**
 * 학생 수강 이력 기반 과목 필터링 유틸리티
 */
object TakenCoursesFilter {

    /**
     * rusaint 수강 이력에서 기본 과목코드 Set 추출
     * @param subjectCodes 8자리 문자열 과목코드 목록
     * @return 기본 과목코드 Set
     */
    fun extractBaseCodes(subjectCodes: List<String>): Set<Long> {
        return subjectCodes
            .mapNotNull { it.toLongOrNull() }
            .toSet()
    }

    /**
     * 이미 수강한 과목 제외
     * @param courses 개설 과목 목록
     * @param takenBaseCodes 수강한 기본 과목코드 Set
     * @return 수강하지 않은 과목 목록
     */
    fun excludeTakenCourses(
        courses: List<Course>,
        takenBaseCodes: Set<Long>
    ): List<Course> {
        return courses.filter { course ->
            course.baseCode() !in takenBaseCodes
        }
    }
}
