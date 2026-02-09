package com.yourssu.soongpt.domain.course.implement

/**
 * 교과교육 계열
 */
enum class SubjectCategory(
    val displayName: String,
    val departments: List<String>,
) {
    INDUSTRIAL("공업교과", listOf("IT융합전공", "전기공학부", "전자정보공학부", "화학공학과")),
    COMMERCIAL("상업교과", listOf("경영학부", "글로벌통상학과", "컴퓨터학부", "회계학과", "회계세무학과")),
    SOCIAL("사회교과", listOf("경제학과", "사학과", "행정학부", "정치외교학과")),
    KOREAN("국어교과", listOf("국어국문학과")),
    FOREIGN("외국어교과", listOf("독어독문학과", "불어불문학과", "영어영문학과", "일어일문학과", "중어중문학과")),
    SCIENCE("과학교과", listOf("물리학과", "화학과")),
    MATH("수학교과", listOf("수학과")),
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
