package com.yourssu.soongpt.domain.course.implement

/**
 * 과목 정보와 수강 대상 학년 정보를 함께 담는 도메인 모델
 * - Target 테이블의 grade1~5 정보를 List<Int>로 변환하여 제공
 */
data class CourseWithTarget(
    val course: Course,
    val targetGrades: List<Int>,
) {
    /**
     * 사용자 학년이 대상 학년에 포함되는지 확인
     * - 포함되면 ON_TIME (정상 수강)
     * - 포함 안 되면 LATE (늦은 수강)
     */
    fun isLateFor(userGrade: Int): Boolean = userGrade !in targetGrades

    companion object {
        /**
         * grade1~5 boolean 값에서 대상 학년 리스트 추출
         */
        fun extractTargetGrades(
            grade1: Boolean,
            grade2: Boolean,
            grade3: Boolean,
            grade4: Boolean,
            grade5: Boolean,
        ): List<Int> = buildList {
            if (grade1) add(1)
            if (grade2) add(2)
            if (grade3) add(3)
            if (grade4) add(4)
            if (grade5) add(5)
        }
    }
}
