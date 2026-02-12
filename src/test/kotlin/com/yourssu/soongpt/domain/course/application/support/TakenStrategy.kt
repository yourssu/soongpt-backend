package com.yourssu.soongpt.domain.course.application.support

/**
 * 기이수 과목 자동 선별 전략 (MockSessionBuilder용).
 * DB에서 조회한 과목 목록을 학년(past/current) 기준으로 나눈 뒤, 전략에 따라 "들은 과목"을 선별한다.
 */
enum class TakenStrategy {
    /** 아무것도 안 들음 */
    NONE,

    /** 전부 이수 */
    ALL,

    /** 현재 학년 이하 과목 중 일부만 이수 (과거 학년 전부 + 현재 학년 절반) */
    PARTIAL_ON_TIME,

    /** 이미 지나간 학년 과목 일부 미이수 → LATE 과목 발생 */
    PARTIAL_LATE,

    /** 거의 다 이수 (1~2개 남김) */
    MOST,

    /** 특정 코드 직접 지정 (하드코딩 필요 시) */
    SPECIFIC_CODES,
}
