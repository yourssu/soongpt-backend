package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes

/**
 * 과목의 권장 학년 대비 수강 시점 상태
 * - LATE: 권장 학년이 지났으나 미이수
 * - ON_TIME: 현재 학년에 해당하는 과목
 */
enum class CourseTiming {
    LATE,
    ON_TIME;

    companion object {
        fun of(targetGrade: Int, userGrade: Int): CourseTiming {
            return if (targetGrade < userGrade) LATE else ON_TIME
        }
    }
}

/**
 * 전공 과목 추천 응답
 */
data class MajorCourseRecommendResponse(
    val category: String,
    val progress: String?,
    val satisfied: Boolean,
    val courses: List<RecommendedCourseResponse>,
    val gradeGroups: List<GradeGroupResponse>? = null,
    val message: String? = null,
) {
    companion object {
        fun of(
            category: Category,
            progress: String?,
            satisfied: Boolean,
            courses: List<RecommendedCourseResponse>,
            gradeGroups: List<GradeGroupResponse>? = null,
            message: String? = null,
        ): MajorCourseRecommendResponse {
            return MajorCourseRecommendResponse(
                category = category.displayName,
                progress = progress,
                satisfied = satisfied,
                courses = courses,
                gradeGroups = gradeGroups,
                message = message,
            )
        }

        fun satisfied(category: Category, progress: String?): MajorCourseRecommendResponse {
            val message = when (category) {
                Category.MAJOR_BASIC -> "전공기초 학점을 이미 모두 이수하셨습니다."
                Category.MAJOR_REQUIRED -> "전공필수 학점을 이미 모두 이수하셨습니다."
                Category.MAJOR_ELECTIVE -> "전공선택 학점을 이미 모두 이수하셨습니다."
                else -> "이미 모두 이수하셨습니다."
            }
            return MajorCourseRecommendResponse(
                category = category.displayName,
                progress = progress,
                satisfied = true,
                courses = emptyList(),
                message = message,
            )
        }

        fun empty(category: Category, progress: String?): MajorCourseRecommendResponse {
            val message = when (category) {
                Category.MAJOR_BASIC -> "이번 학기에 수강 가능한 전공기초 과목이 없습니다."
                Category.MAJOR_REQUIRED -> "이번 학기에 수강 가능한 전공필수 과목이 없습니다."
                Category.MAJOR_ELECTIVE -> "이번 학기에 수강 가능한 전공선택 과목이 없습니다."
                else -> "이번 학기에 수강 가능한 과목이 없습니다."
            }
            return MajorCourseRecommendResponse(
                category = category.displayName,
                progress = progress,
                satisfied = false,
                courses = emptyList(),
                message = message,
            )
        }
    }
}

/**
 * 학년별 그룹 (전선용)
 */
data class GradeGroupResponse(
    val grade: Int,
    val courses: List<RecommendedCourseResponse>,
)

/**
 * 추천 과목 (분반 그룹핑)
 */
data class RecommendedCourseResponse(
    val baseCourseCode: Long,
    val courseName: String,
    val credits: Double?,
    val targetGrade: Int,
    val timing: CourseTiming,
    val sections: List<SectionResponse>,
) {
    companion object {
        fun from(
            courses: List<Course>,
            targetGrade: Int,
            userGrade: Int,
        ): RecommendedCourseResponse {
            val representative = courses.first()
            return RecommendedCourseResponse(
                baseCourseCode = representative.baseCode(),
                courseName = representative.name,
                credits = representative.credit,
                targetGrade = targetGrade,
                timing = CourseTiming.of(targetGrade, userGrade),
                sections = courses.map { SectionResponse.from(it) },
            )
        }
    }
}

/**
 * 분반 정보
 */
data class SectionResponse(
    val courseCode: Long,
    val professor: String?,
    val schedule: String,
) {
    companion object {
        fun from(course: Course): SectionResponse {
            val courseTimes = CourseTimes.from(course.scheduleRoom).toList()
            val schedule = if (courseTimes.isNotEmpty()) {
                courseTimes.joinToString(", ") {
                    "${it.week.displayName}${it.startTime.toTimeFormat()}-${it.endTime.toTimeFormat()}"
                }
            } else {
                course.scheduleRoom
            }

            return SectionResponse(
                courseCode = course.code,
                professor = course.professor,
                schedule = schedule,
            )
        }
    }
}
