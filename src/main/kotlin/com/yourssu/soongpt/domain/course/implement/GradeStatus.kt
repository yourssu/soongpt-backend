package com.yourssu.soongpt.domain.course.implement

/**
 * 과목의 학년 상태 분류
 * - PAST_DUE: 권장 학년이 지났으나 미이수
 * - CURRENT: 현재 학년에 해당하는 과목
 */
enum class GradeStatus {
    PAST_DUE,
    CURRENT;

    companion object {
        /**
         * 과목의 권장 학년과 사용자 학년을 비교하여 상태 결정
         * @param targetGrade 과목의 권장 이수 학년
         * @param userGrade 사용자의 현재 학년
         */
        fun of(targetGrade: Int, userGrade: Int): GradeStatus {
            return if (targetGrade < userGrade) PAST_DUE else CURRENT
        }
    }
}
