package com.yourssu.soongpt.domain.course.business.dto

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.TeachingMajorArea
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.scheduleWithoutRoom
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto

/**
 * 과목의 권장 학년 대비 수강 시점 상태
 * - LATE: 대상 학년이 지났으나 미이수
 * - ON_TIME: 현재 학년이 대상 학년에 포함됨
 */
enum class CourseTiming {
    LATE,
    ON_TIME;
}

/**
 * 졸업사정표 학점 이수 현황
 */
data class Progress(
    val required: Int?,
    val completed: Int?,
    val satisfied: Boolean,
) {
    companion object {
        fun from(summary: RusaintCreditSummaryItemDto): Progress {
            return Progress(
                required = summary.required,
                completed = summary.completed,
                satisfied = summary.satisfied,
            )
        }
    }
}

/**
 * 전공 과목 추천 응답
 */
data class MajorCourseRecommendResponse(
    val category: String, // MAJOR_BASIC, MAJOR_REQUIRED, MAJOR_ELECTIVE, RETAKE
    val progress: Progress?,
    val courses: List<RecommendedCourseResponse>,
    val message: String? = null,
) {
    companion object {
        fun of(
            category: Category,
            progress: Progress,
            courses: List<RecommendedCourseResponse>,
            message: String? = null,
        ): MajorCourseRecommendResponse {
            return MajorCourseRecommendResponse(
                category = category.name,
                progress = progress,
                courses = courses,
                message = message,
            )
        }

        fun satisfied(category: Category, progress: Progress): MajorCourseRecommendResponse {
            val message = when (category) {
                Category.MAJOR_BASIC -> "전공기초 학점을 이미 모두 이수하셨습니다."
                Category.MAJOR_REQUIRED -> "전공필수 학점을 이미 모두 이수하셨습니다."
                Category.MAJOR_ELECTIVE -> "전공선택 학점을 이미 모두 이수하셨습니다."
                else -> "이미 모두 이수하셨습니다."
            }
            return MajorCourseRecommendResponse(
                category = category.name,
                progress = progress,
                courses = emptyList(),
                message = message,
            )
        }

        fun empty(category: Category, progress: Progress): MajorCourseRecommendResponse {
            val message = when (category) {
                Category.MAJOR_BASIC -> "이번 학기에 수강 가능한 전공기초 과목이 없습니다."
                Category.MAJOR_REQUIRED -> "이번 학기에 수강 가능한 전공필수 과목이 없습니다."
                Category.MAJOR_ELECTIVE -> "이번 학기에 수강 가능한 전공선택 과목이 없습니다."
                else -> "이번 학기에 수강 가능한 과목이 없습니다."
            }
            return MajorCourseRecommendResponse(
                category = category.name,
                progress = progress,
                courses = emptyList(),
                message = message,
            )
        }

    }
}

/**
 * 추천 과목 (분반 그룹핑)
 */
data class RecommendedCourseResponse(
    val baseCourseCode: Long,
    val courseName: String,
    val credits: Double?,
    val target: String,
    val targetGrades: List<Int> = emptyList(),
    val isCrossMajor: Boolean = false,
    val timing: CourseTiming?,
    val field: String? = null,
    val professors: List<String>,
    val department: String?,
    val sections: List<SectionResponse>,
) {
    companion object {
        fun from(
            coursesWithTarget: List<com.yourssu.soongpt.domain.course.implement.CourseWithTarget>,
            isLate: Boolean,
            field: String? = null,
            isCrossMajor: Boolean = false,
        ): RecommendedCourseResponse {
            val representative = coursesWithTarget.first()
            val courses = coursesWithTarget.map { it.course }
            val professors = courses
                .mapNotNull { it.professor }
                .distinct()
                .sorted()

            // 전공과목(전기/전필/전선), 타전공인정, 복수전공/부전공(field=복필/복선/부필/부선)일 경우 department 포함
            val department = when {
                isCrossMajor -> representative.course.department
                representative.course.category in listOf(Category.MAJOR_BASIC, Category.MAJOR_REQUIRED, Category.MAJOR_ELECTIVE) -> representative.course.department
                field in listOf("복필", "복선", "부필", "부선") -> representative.course.department
                else -> null
            }

            return RecommendedCourseResponse(
                baseCourseCode = representative.course.baseCode(),
                courseName = representative.course.name,
                credits = representative.course.credit,
                target = representative.course.target,
                targetGrades = representative.targetGrades,
                isCrossMajor = isCrossMajor,
                timing = if (isLate) CourseTiming.LATE else CourseTiming.ON_TIME,
                field = field,
                professors = professors,
                department = department,
                sections = coursesWithTarget.map { SectionResponse.from(it.course, it.isStrict, divisionFromCourseCode = true) },
            )
        }

        fun forRetake(
            coursesWithTarget: List<com.yourssu.soongpt.domain.course.implement.CourseWithTarget>,
        ): RecommendedCourseResponse {
            val representative = coursesWithTarget.first()
            val courses = coursesWithTarget.map { it.course }
            val professors = courses
                .mapNotNull { it.professor }
                .distinct()
                .sorted()

            return RecommendedCourseResponse(
                baseCourseCode = representative.course.baseCode(),
                courseName = representative.course.name,
                credits = representative.course.credit,
                target = representative.course.target,
                targetGrades = representative.targetGrades,
                timing = null,
                professors = professors,
                department = representative.course.department,
                sections = coursesWithTarget.map { SectionResponse.from(it.course, it.isStrict, divisionFromCourseCode = true) },
            )
        }

        fun forTeaching(
            courses: List<Course>,
            area: TeachingMajorArea?,
        ): RecommendedCourseResponse {
            val representative = courses.first()
            val professors = courses
                .mapNotNull { it.professor }
                .distinct()
                .sorted()

            return RecommendedCourseResponse(
                baseCourseCode = representative.baseCode(),
                courseName = representative.name,
                credits = representative.credit,
                target = representative.target,
                timing = null,
                field = area?.displayName,
                professors = professors,
                department = null,
                sections = courses.map { SectionResponse.from(it, divisionFromCourseCode = true) },
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
    val division: String?,
    val schedule: String,
    val isStrictRestriction: Boolean,
) {
    companion object {
        fun from(
            course: Course,
            isStrictRestriction: Boolean = false,
            divisionFromCourseCode: Boolean = false,
        ): SectionResponse {
            val division = when {
                divisionFromCourseCode -> "%02d".format(course.code % 100)
                else -> course.division
            }
            return SectionResponse(
                courseCode = course.code,
                professor = course.professor,
                division = division,
                schedule = course.scheduleWithoutRoom(),
                isStrictRestriction = isStrictRestriction,
            )
        }
    }
}
