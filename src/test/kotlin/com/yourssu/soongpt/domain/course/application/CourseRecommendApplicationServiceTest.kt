package com.yourssu.soongpt.domain.course.application

import com.yourssu.soongpt.domain.course.application.dto.RecommendCoursesRequest
import com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendService
import com.yourssu.soongpt.domain.course.business.MajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.RetakeCourseRecommendService
import com.yourssu.soongpt.domain.course.business.SecondaryMajorCourseRecommendService
import com.yourssu.soongpt.domain.course.business.TeachingCourseRecommendService
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResponse
import com.yourssu.soongpt.domain.course.business.dto.CategoryRecommendResult
import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintChapelSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintCreditSummaryItemDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintGraduationSummaryDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import jakarta.servlet.http.HttpServletRequest
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.whenever

class CourseRecommendApplicationServiceTest : BehaviorSpec({

    val contextResolver = mock<RecommendContextResolver>()
    val majorCourseRecommendService = mock<MajorCourseRecommendService>()
    val generalCourseRecommendService = mock<GeneralCourseRecommendService>()
    val retakeCourseRecommendService = mock<RetakeCourseRecommendService>()
    val secondaryMajorCourseRecommendService = mock<SecondaryMajorCourseRecommendService>()
    val teachingCourseRecommendService = mock<TeachingCourseRecommendService>()

    val service = CourseRecommendApplicationService(
        contextResolver = contextResolver,
        majorCourseRecommendService = majorCourseRecommendService,
        generalCourseRecommendService = generalCourseRecommendService,
        retakeCourseRecommendService = retakeCourseRecommendService,
        secondaryMajorCourseRecommendService = secondaryMajorCourseRecommendService,
        teachingCourseRecommendService = teachingCourseRecommendService,
    )

    val request = mock<HttpServletRequest>()

    fun credit(required: Int = 12, completed: Int = 6, satisfied: Boolean = false): RusaintCreditSummaryItemDto {
        return RusaintCreditSummaryItemDto(
            required = required,
            completed = completed,
            satisfied = satisfied,
        )
    }

    fun summary(
        generalRequired: RusaintCreditSummaryItemDto = credit(),
        majorFoundation: RusaintCreditSummaryItemDto = credit(),
        majorRequired: RusaintCreditSummaryItemDto = credit(),
        majorElective: RusaintCreditSummaryItemDto = credit(),
        minor: RusaintCreditSummaryItemDto = credit(),
        doubleMajorRequired: RusaintCreditSummaryItemDto = credit(),
        doubleMajorElective: RusaintCreditSummaryItemDto = credit(),
    ): RusaintGraduationSummaryDto {
        return RusaintGraduationSummaryDto(
            generalRequired = generalRequired,
            generalElective = credit(),
            majorFoundation = majorFoundation,
            majorRequired = majorRequired,
            majorElective = majorElective,
            minor = minor,
            doubleMajorRequired = doubleMajorRequired,
            doubleMajorElective = doubleMajorElective,
            christianCourses = credit(),
            chapel = RusaintChapelSummaryItemDto(satisfied = false),
        )
    }

    fun context(
        graduationSummary: RusaintGraduationSummaryDto? = summary(),
        lowGradeSubjectCodes: List<String> = listOf("21500147"),
        warnings: List<String> = listOf("동기화 경고"),
    ): RecommendContext {
        return RecommendContext(
            departmentName = "컴퓨터학부",
            userGrade = 3,
            schoolId = 23,
            admissionYear = 2023,
            takenSubjectCodes = listOf("21500118", "21500234"),
            lowGradeSubjectCodes = lowGradeSubjectCodes,
            graduationSummary = graduationSummary,
            graduationRequirements = null,
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = "글로벌미디어학부",
                minorDepartment = "수학과",
                teaching = true,
            ),
            warnings = warnings,
        )
    }

    fun categoryResponse(
        category: String,
        progress: Progress? = null,
        message: String? = null,
    ): CategoryRecommendResponse {
        val safeProgress = progress ?: when (category) {
            // 재수강/교직은 졸업사정표 기반 progress bar가 없으므로 센티널(-1) 사용
            "RETAKE", "TEACHING" -> Progress.notApplicable()
            // 테스트에서 progress를 명시하지 않은 경우를 위한 더미 값
            else -> Progress.from(credit())
        }
        return CategoryRecommendResponse(
            category = category,
            progress = safeProgress,
            message = message,
            userGrade = null,
            courses = emptyList(),
            lateFields = null,
        )
    }

    beforeTest {
        reset(
            contextResolver,
            majorCourseRecommendService,
            generalCourseRecommendService,
            retakeCourseRecommendService,
            secondaryMajorCourseRecommendService,
            teachingCourseRecommendService,
        )
        whenever(contextResolver.resolve(request)).thenReturn(context())
    }

    given("MAJOR_BASIC 요청") {
        `when`("졸업사정표 majorFoundation이 있으면") {
            then("major 서비스로 라우팅하고 결과를 CategoryRecommendResponse로 변환한다") {
                val majorFoundation = credit(required = 15, completed = 9, satisfied = false)
                val expectedProgress = Progress(required = 15, completed = 9, satisfied = false)

                whenever(contextResolver.resolve(request)).thenReturn(
                    context(
                        graduationSummary = summary(majorFoundation = majorFoundation),
                        warnings = listOf("warn-1"),
                    )
                )
                whenever(
                    majorCourseRecommendService.recommendMajorBasicOrRequired(
                        departmentName = "컴퓨터학부",
                        userGrade = 3,
                        category = Category.MAJOR_BASIC,
                        takenSubjectCodes = listOf("21500118", "21500234"),
                        progress = expectedProgress,
                    )
                ).thenReturn(
                    CategoryRecommendResult(
                        category = "MAJOR_BASIC",
                        progress = expectedProgress,
                        courses = emptyList(),
                        message = "전공기초 추천 결과",
                    )
                )

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "MAJOR_BASIC"),
                )

                result.warnings shouldContainExactly listOf("warn-1")
                result.categories shouldHaveSize 1
                result.categories.first() shouldBe categoryResponse(
                    category = "MAJOR_BASIC",
                    progress = expectedProgress,
                    message = "전공기초 추천 결과",
                )

                verify(majorCourseRecommendService, times(1)).recommendMajorBasicOrRequired(
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    category = Category.MAJOR_BASIC,
                    takenSubjectCodes = listOf("21500118", "21500234"),
                    progress = expectedProgress,
                )
                verifyNoInteractions(
                    generalCourseRecommendService,
                    retakeCourseRecommendService,
                    secondaryMajorCourseRecommendService,
                    teachingCourseRecommendService,
                )
            }
        }
    }

    given("MAJOR_REQUIRED 요청") {
        `when`("졸업사정표가 없으면") {
            then("progress만 unavailable(-2,-2,false)이고 major 서비스로 추천 과목을 조회한다") {
                whenever(contextResolver.resolve(request)).thenReturn(
                    context(graduationSummary = null)
                )
                whenever(
                    majorCourseRecommendService.recommendMajorBasicOrRequired(
                        departmentName = "컴퓨터학부",
                        userGrade = 3,
                        category = Category.MAJOR_REQUIRED,
                        takenSubjectCodes = listOf("21500118", "21500234"),
                        progress = Progress.unavailable(),
                    )
                ).thenReturn(
                    CategoryRecommendResult.of(
                        category = Category.MAJOR_REQUIRED,
                        progress = Progress.unavailable(),
                        courses = emptyList(),
                        message = "이번 학기에 수강 가능한 전공필수 과목이 없습니다.",
                    )
                )

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "MAJOR_REQUIRED"),
                )

                result.categories shouldHaveSize 1
                result.categories.first().progress.required shouldBe -2
                result.categories.first().progress.completed shouldBe -2
                result.categories.first().progress.satisfied shouldBe false

                verify(majorCourseRecommendService, times(1)).recommendMajorBasicOrRequired(
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    category = Category.MAJOR_REQUIRED,
                    takenSubjectCodes = listOf("21500118", "21500234"),
                    progress = Progress.unavailable(),
                )
            }
        }
    }

    given("GENERAL_REQUIRED 요청") {
        `when`("졸업사정표 generalRequired가 있으면") {
            then("general 서비스로 라우팅하면서 schoolId/admissionYear를 전달한다") {
                val generalRequired = credit(required = 14, completed = 7, satisfied = false)
                val expectedProgress = Progress(required = 14, completed = 7, satisfied = false)
                val expectedResponse = categoryResponse(
                    category = "GENERAL_REQUIRED",
                    progress = expectedProgress,
                    message = "교필 추천 결과",
                )

                whenever(contextResolver.resolve(request)).thenReturn(
                    context(graduationSummary = summary(generalRequired = generalRequired))
                )
                whenever(
                    generalCourseRecommendService.recommend(
                        category = Category.GENERAL_REQUIRED,
                        departmentName = "컴퓨터학부",
                        userGrade = 3,
                        schoolId = 23,
                        admissionYear = 2023,
                        takenSubjectCodes = listOf("21500118", "21500234"),
                        progress = expectedProgress,
                    )
                ).thenReturn(expectedResponse)

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "GENERAL_REQUIRED"),
                )

                result.categories shouldContainExactly listOf(expectedResponse)
                verify(generalCourseRecommendService, times(1)).recommend(
                    category = Category.GENERAL_REQUIRED,
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    schoolId = 23,
                    admissionYear = 2023,
                    takenSubjectCodes = listOf("21500118", "21500234"),
                    progress = expectedProgress,
                )
            }
        }
    }

    given("RETAKE 요청") {
        `when`("카테고리가 RETAKE면") {
            then("retake 서비스로 라우팅하고 lowGradeSubjectCodes를 전달한다") {
                val expectedResponse = categoryResponse(
                    category = "RETAKE",
                    progress = null,
                    message = "재수강 추천 결과",
                )
                whenever(contextResolver.resolve(request)).thenReturn(
                    context(lowGradeSubjectCodes = listOf("21599999"))
                )
                whenever(
                    retakeCourseRecommendService.recommend(
                        lowGradeSubjectCodes = listOf("21599999"),
                    )
                ).thenReturn(expectedResponse)

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "RETAKE"),
                )

                result.categories shouldContainExactly listOf(expectedResponse)
                verify(retakeCourseRecommendService, times(1)).recommend(
                    lowGradeSubjectCodes = listOf("21599999"),
                )
            }
        }
    }

    given("SECONDARY/TEACHING 카테고리 요청") {
        `when`("DOUBLE_MAJOR_REQUIRED를 요청하면") {
            then("secondaryMajor 서비스의 복수전공필수 메서드를 호출한다") {
                val expectedResponse = categoryResponse(category = "DOUBLE_MAJOR_REQUIRED")
                whenever(
                    secondaryMajorCourseRecommendService.recommendDoubleMajorRequired(context())
                ).thenReturn(expectedResponse)

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "DOUBLE_MAJOR_REQUIRED"),
                )

                result.categories shouldContainExactly listOf(expectedResponse)
                verify(secondaryMajorCourseRecommendService, times(1)).recommendDoubleMajorRequired(context())
            }
        }

        `when`("DOUBLE_MAJOR_ELECTIVE를 요청하면") {
            then("secondaryMajor 서비스의 복수전공선택 메서드를 호출한다") {
                val expectedResponse = categoryResponse(category = "DOUBLE_MAJOR_ELECTIVE")
                whenever(
                    secondaryMajorCourseRecommendService.recommendDoubleMajorElective(context())
                ).thenReturn(expectedResponse)

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "DOUBLE_MAJOR_ELECTIVE"),
                )

                result.categories shouldContainExactly listOf(expectedResponse)
                verify(secondaryMajorCourseRecommendService, times(1)).recommendDoubleMajorElective(context())
            }
        }

        `when`("MINOR를 요청하면") {
            then("secondaryMajor 서비스의 부전공 메서드를 호출한다") {
                val expectedResponse = categoryResponse(category = "MINOR")
                whenever(
                    secondaryMajorCourseRecommendService.recommendMinor(context())
                ).thenReturn(expectedResponse)

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "MINOR"),
                )

                result.categories shouldContainExactly listOf(expectedResponse)
                verify(secondaryMajorCourseRecommendService, times(1)).recommendMinor(context())
            }
        }

        `when`("TEACHING을 요청하면") {
            then("teaching 서비스로 라우팅한다") {
                val expectedResponse = categoryResponse(category = "TEACHING")
                whenever(
                    teachingCourseRecommendService.recommend(context())
                ).thenReturn(expectedResponse)

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "TEACHING"),
                )

                result.categories shouldContainExactly listOf(expectedResponse)
                verify(teachingCourseRecommendService, times(1)).recommend(context())
            }
        }
    }

    given("복수 카테고리 요청") {
        `when`("RETAKE,MAJOR_BASIC,TEACHING 순서로 요청하면") {
            then("dispatch 결과도 동일한 순서를 유지한다") {
                val majorProgress = Progress(required = 12, completed = 6, satisfied = false)

                whenever(
                    retakeCourseRecommendService.recommend(
                        lowGradeSubjectCodes = listOf("21500147"),
                    )
                ).thenReturn(categoryResponse(category = "RETAKE", message = "retake"))

                whenever(
                    majorCourseRecommendService.recommendMajorBasicOrRequired(
                        departmentName = "컴퓨터학부",
                        userGrade = 3,
                        category = Category.MAJOR_BASIC,
                        takenSubjectCodes = listOf("21500118", "21500234"),
                        progress = majorProgress,
                    )
                ).thenReturn(
                    CategoryRecommendResult(
                        category = "MAJOR_BASIC",
                        progress = majorProgress,
                        courses = emptyList(),
                        message = "major-basic",
                    )
                )

                whenever(
                    teachingCourseRecommendService.recommend(context())
                ).thenReturn(categoryResponse(category = "TEACHING", message = "teaching"))

                val result = service.recommend(
                    request = request,
                    query = RecommendCoursesRequest(category = "RETAKE,MAJOR_BASIC,TEACHING"),
                )

                result.categories.map { it.category } shouldContainExactly listOf(
                    "RETAKE",
                    "MAJOR_BASIC",
                    "TEACHING",
                )
            }
        }
    }
})
