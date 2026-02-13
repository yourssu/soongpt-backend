package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.application.RecommendContext
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SecondaryMajorCourseRecommendServiceTest : BehaviorSpec({

    val courseRepository = mock<CourseRepository>()
    val departmentReader = mock<DepartmentReader>()
    val service = SecondaryMajorCourseRecommendService(courseRepository, departmentReader)

    fun context(
        doubleMajorDepartment: String? = null,
        minorDepartment: String? = null,
        doubleMajorRequired: RusaintCreditSummaryItemDto? = null,
        doubleMajorElective: RusaintCreditSummaryItemDto? = null,
        minor: RusaintCreditSummaryItemDto? = null,
    ): RecommendContext {
        val summary = if (doubleMajorRequired != null || doubleMajorElective != null || minor != null) {
            RusaintGraduationSummaryDto(
                generalRequired = RusaintCreditSummaryItemDto(0, 0, true),
                generalElective = RusaintCreditSummaryItemDto(0, 0, true),
                majorFoundation = RusaintCreditSummaryItemDto(0, 0, true),
                majorRequired = RusaintCreditSummaryItemDto(0, 0, true),
                majorElective = RusaintCreditSummaryItemDto(0, 0, true),
                minor = minor ?: RusaintCreditSummaryItemDto(0, 0, true),
                doubleMajorRequired = doubleMajorRequired ?: RusaintCreditSummaryItemDto(0, 0, true),
                doubleMajorElective = doubleMajorElective ?: RusaintCreditSummaryItemDto(0, 0, true),
                christianCourses = RusaintCreditSummaryItemDto(0, 0, true),
                chapel = com.yourssu.soongpt.domain.usaint.implement.dto.RusaintChapelSummaryItemDto(satisfied = true),
            )
        } else null
        return RecommendContext(
            departmentName = "컴퓨터학부",
            userGrade = 3,
            schoolId = 23,
            admissionYear = 2023,
            takenSubjectCodes = emptyList(),
            lowGradeSubjectCodes = emptyList(),
            graduationSummary = summary,
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = doubleMajorDepartment,
                minorDepartment = minorDepartment,
                teaching = false,
            ),
            warnings = emptyList(),
        )
    }

    given("복수전공필수 - 복수전공 미등록") {
        `when`("doubleMajorDepartment가 null이면") {
            val ctx = context(doubleMajorDepartment = null)
            val result = service.recommendDoubleMajorRequired(ctx)

            then("등록 안내 메시지와 빈 courses를 반환한다") {
                result.category shouldBe "DOUBLE_MAJOR_REQUIRED"
                result.progress.required shouldBe 0
                result.progress.completed shouldBe 0
                result.progress.satisfied shouldBe true
                result.message shouldBe "복수전공을 등록하지 않았습니다."
                result.courses shouldHaveSize 0
            }
        }
    }

    given("복수전공필수 - 졸업사정표에 항목 없음") {
        `when`("graduationSummary가 null이면") {
            val ctx = context(doubleMajorDepartment = "경영학부").copy(graduationSummary = null)
            val result = service.recommendDoubleMajorRequired(ctx)

            then("noData 메시지를 반환한다") {
                result.message shouldBe "졸업사정표에 복수전공필수 항목이 없습니다."
                result.progress.required shouldBe 0
                result.progress.completed shouldBe 0
                result.progress.satisfied shouldBe true
                result.courses shouldHaveSize 0
            }
        }
    }

    given("복수전공선택 - 복수전공 미등록") {
        `when`("doubleMajorDepartment가 null이면") {
            val ctx = context(doubleMajorDepartment = null)
            val result = service.recommendDoubleMajorElective(ctx)

            then("등록 안내 메시지를 반환한다") {
                result.message shouldBe "복수전공을 등록하지 않았습니다."
                result.courses shouldHaveSize 0
            }
        }
    }

    given("부전공 - 부전공 미등록") {
        `when`("minorDepartment가 null이면") {
            val ctx = context(minorDepartment = null)
            val result = service.recommendMinor(ctx)

            then("등록 안내 메시지를 반환한다") {
                result.category shouldBe "MINOR"
                result.message shouldBe "부전공을 등록하지 않았습니다."
                result.courses shouldHaveSize 0
            }
        }
    }
})
