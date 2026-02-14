package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.RecommendedCourseResponse
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.baseCode
import org.springframework.stereotype.Service

/**
 * 재수강 과목 추천 서비스
 * - C 이하 성적의 과목 중 이번 학기 개설 과목을 추천
 * - 폐강된 교양필수(구과목)는 DB에 없으므로, 구과목 baseCode → 대체 신과목 baseCode 매핑으로 조회
 */
@Service
class RetakeCourseRecommendService(
    private val courseRepository: CourseRepository,
) {

    fun recommend(lowGradeSubjectCodes: List<String>): CategoryRecommendResponse {
        if (lowGradeSubjectCodes.isEmpty()) {
            return retakeEmpty("재수강 가능한 C+ 이하 과목이 없습니다.")
        }

        val baseCodes = lowGradeSubjectCodes.mapNotNull { it.toLongOrNull() }
        if (baseCodes.isEmpty()) {
            return retakeEmpty("재수강 가능한 C+ 이하 과목이 없습니다.")
        }

        // 폐강 구과목 baseCode → 대체 신과목 baseCode (재수강교필_하드코딩.md). 구과목은 DB에 없으므로 치환 후 조회
        val replacementCodes = baseCodes.mapNotNull { RETAKE_OLD_TO_REPLACEMENT[it] }
        val codesToQuery = (baseCodes + replacementCodes).distinct()
        val coursesWithTarget = courseRepository.findCoursesWithTargetByBaseCodes(codesToQuery)

        if (coursesWithTarget.isEmpty()) {
            return retakeEmpty(
                "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
            )
        }

        // target 조인으로 같은 분반(10자리)이 학과별로 여러 행 나올 수 있음 → 분반 단위로 한 번만 사용
        val recommendedCourses = coursesWithTarget
            .groupBy { it.course.baseCode() }
            .map { (_, sections) ->
                RecommendedCourseResponse.forRetake(sections.distinctBy { it.course.code })
            }
            .sortedBy { it.courseName }

        return CategoryRecommendResponse(
            category = RETAKE_CATEGORY,
            progress = Progress.notApplicable(),
            message = null,
            userGrade = null,
            courses = recommendedCourses,
            lateFields = null,
        )
    }

    private fun retakeEmpty(message: String) = CategoryRecommendResponse(
        category = RETAKE_CATEGORY,
        progress = Progress.notApplicable(),
        message = message,
        userGrade = null,
        courses = emptyList(),
        lateFields = null,
    )

    companion object {
        private const val RETAKE_CATEGORY = "RETAKE"

        /**
         * 교양필수 재수강: 폐강된 구과목 baseCode(8자리) → 대체 신과목 baseCode(8자리).
         * 구과목은 현재 학기 DB에 없으므로, lowGradeSubjectCodes에 구과목 코드가 있으면 이 맵으로 치환해 조회한다.
         * 재수강교필_하드코딩.md 기준. (rusaint/학사에서 내려오는 구과목 코드 → 대체 과목 baseCode)
         */
        private val RETAKE_OLD_TO_REPLACEMENT: Map<Long, Long> = mapOf(
            // 독서와토론 → [인문적상상력과소통] 고전읽기와상상력 (21501003)
            21506685L to 21501003L,
            21500902L to 21501003L,
            21500185L to 21501003L,
            21500903L to 21501003L,
            21500904L to 21501003L,
            // 현대인과성서 → [인간과성서] 현대사회이슈와기독교 (21501020)
            21503037L to 21501020L,
            21500183L to 21501020L,
            21500908L to 21501020L,
            21500909L to 21501020L,
            21500910L to 21501020L,
            21503000L to 21501020L,
            // 기업가정신과행동 → [창의적사고와혁신] 혁신과기업가정신 (21501009)
            21500898L to 21501009L,
            21500516L to 21501009L,
            21500896L to 21501009L,
            21500897L to 21501009L,
            // 대학글쓰기 → [비판적사고와표현] 미디어사회와비평적글쓰기 (21501006)
            21500901L to 21501006L,
            21500184L to 21501006L,
            21500519L to 21501006L,
            21500899L to 21501006L,
            21500900L to 21501006L,
            21502516L to 21501006L,
            21505220L to 21501006L,
            // 컴퓨터사고 → [컴퓨팅적사고] 컴퓨팅적사고와코딩기초 (21501028)
            21500907L to 21501028L,
            21500188L to 21501028L,
            21500905L to 21501028L,
            21500906L to 21501028L,
            // AI와데이터사회 → [SW와AI] AI와데이터기초 (21501034)
            21500747L to 21501034L,
            21500582L to 21501034L,
            21500893L to 21501034L,
            21500894L to 21501034L,
            21500895L to 21501034L,
        )
    }
}
