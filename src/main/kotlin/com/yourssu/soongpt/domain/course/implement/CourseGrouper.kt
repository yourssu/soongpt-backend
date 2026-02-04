package com.yourssu.soongpt.domain.course.implement

/**
 * 같은 과목의 분반을 그룹핑하는 유틸리티
 */
object CourseGrouper {

    /**
     * 기본 과목코드 기준으로 분반 그룹핑
     * @param courses 과목 목록
     * @return Map<기본코드, 분반 목록>
     */
    fun groupByBaseCode(courses: List<Course>): Map<Long, List<Course>> {
        return courses.groupBy { it.baseCode() }
    }
}
