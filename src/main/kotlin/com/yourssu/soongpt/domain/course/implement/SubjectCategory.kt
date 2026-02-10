package com.yourssu.soongpt.domain.course.implement

/**
 * 교과교육 계열
 * 단과대학별 설치 학과 및 표시과목
 */
enum class SubjectCategory(
    val displayName: String,
    val departments: List<String>,
) {
    // 공업교과: 화공, 전기, 전자, 통신
    INDUSTRIAL("공업교과", listOf("IT융합전공", "전기공학부", "전자정보공학부", "화학공학과")),
    // 상업교과: 상업, 정보·컴퓨터
    COMMERCIAL("상업교과", listOf("경영학부", "글로벌통상학과", "컴퓨터학부", "회계학과")),
    // 사회교과: 일반사회, 역사
    SOCIAL("사회교과", listOf("경제학과", "사학과")),
    // 국어교과
    KOREAN("국어교과", listOf("국어국문학과")),
    // 외국어교과: 영어, 독일어, 프랑스어, 중국어, 일본어
    FOREIGN("외국어교과", listOf("독어독문학과", "불어불문학과", "영어영문학과", "일어일문학과", "중어중문학과")),
    // 과학교과: 물리, 화학
    SCIENCE("과학교과", listOf("물리학과", "화학과")),
    // 수학교과
    MATH("수학교과", listOf("수학과")),
    // 철학교과
    PHILOSOPHY("철학교과", listOf("철학과")),
    ;

    companion object {
        /**
         * 학과명으로 교과 계열 찾기
         */
        fun findByDepartment(departmentName: String): SubjectCategory? {
            return entries.firstOrNull { category ->
                category.departments.any { dept -> departmentName.contains(dept) || dept.contains(departmentName) }
            }
        }
    }
}
