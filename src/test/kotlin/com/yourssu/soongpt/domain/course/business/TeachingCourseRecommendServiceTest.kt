package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.application.RecommendContext
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TeachingCourseRecommendServiceTest : BehaviorSpec({

    val departmentReader = mock<DepartmentReader>()
    val targetReader = mock<TargetReader>()
    val courseReader = mock<CourseReader>()

    val service = TeachingCourseRecommendService(
        departmentReader = departmentReader,
        targetReader = targetReader,
        courseReader = courseReader,
    )

    val department = Department(id = 1L, name = "컴퓨터학부", collegeId = 10L)

    fun ctx(
        teaching: Boolean,
        takenSubjectCodes: List<String> = emptyList(),
        schoolId: Int = 26,
    ): RecommendContext {
        return RecommendContext(
            departmentName = "컴퓨터학부",
            userGrade = 3,
            schoolId = schoolId,
            admissionYear = 2023,
            takenSubjectCodes = takenSubjectCodes,
            lowGradeSubjectCodes = emptyList(),
            graduationSummary = null,
            flags = RusaintStudentFlagsDto(
                doubleMajorDepartment = null,
                minorDepartment = null,
                teaching = teaching,
            ),
            warnings = emptyList(),
        )
    }

    fun teachingCourse(
        code: Long,
        name: String,
        field: String,
        scheduleRoom: String = "월 09:00-10:15 (강의실)",
        professor: String? = null,
    ): Course {
        return Course(
            id = null,
            category = Category.TEACHING,
            field = field,
            code = code,
            name = name,
            professor = professor,
            department = "학사팀",
            division = null,
            time = "2.0",
            point = "2",
            personeel = 0,
            scheduleRoom = scheduleRoom,
            target = "",
            credit = 2.0,
        )
    }

    beforeTest {
        reset(departmentReader, targetReader, courseReader)
        whenever(departmentReader.getByName("컴퓨터학부")).thenReturn(department)

        // Kotlin non-null return 타입이라 기본 null 반환 시 NPE가 날 수 있어 기본값을 미리 지정
        (1..5).forEach { grade ->
            whenever(targetReader.findAllByDepartmentGrade(department, grade)).thenReturn(emptyList())
            whenever(targetReader.findAllByDepartmentGradeForTeaching(department, grade)).thenReturn(emptyList())
        }
    }

    given("교직이수 추천") {
        `when`("교직이수 대상이 아니면") {
            then("DB 조회 없이 안내 메시지를 반환한다") {
                val result = service.recommend(ctx(teaching = false))

                result.category shouldBe "TEACHING"
                result.message shouldBe "교직이수 대상이 아닙니다."
                result.courses shouldHaveSize 0

                verifyNoInteractions(departmentReader, targetReader, courseReader)
            }
        }

        `when`("교직이수 대상이지만 모든 과목을 이미 이수했으면") {
            then("빈 과목 메시지를 반환한다") {
                // grade 1에서만 과목코드가 조회되는 케이스로 가정
                whenever(targetReader.findAllByDepartmentGrade(department, 1)).thenReturn(listOf(2150118601L))
                whenever(targetReader.findAllByDepartmentGradeForTeaching(department, 1)).thenReturn(listOf(2150118601L))

                whenever(
                    courseReader.findAllInCategory(
                        category = eq(Category.TEACHING),
                        courseCodes = any(),
                        schoolId = eq(26),
                    )
                ).thenReturn(
                    listOf(
                        teachingCourse(
                            code = 2150118601L,
                            name = "교육학개론",
                            field = "교직영역/교직이론",
                        )
                    )
                )

                val result = service.recommend(
                    ctx(
                        teaching = true,
                        takenSubjectCodes = listOf("2150118601"),
                    )
                )

                result.category shouldBe "TEACHING"
                result.message shouldBe "이번 학기에 수강 가능한 교직 과목이 없습니다."
                result.courses shouldHaveSize 0

                verify(departmentReader, times(1)).getByName("컴퓨터학부")
                verify(targetReader, times(5)).findAllByDepartmentGrade(eq(department), any())
                verify(targetReader, times(5)).findAllByDepartmentGradeForTeaching(eq(department), any())
            }
        }

        `when`("전공/교직/특성화 과목이 모두 있으면") {
            then("영역 순서(전공→교직→특성화)로 그룹핑되어 반환한다") {
                whenever(targetReader.findAllByDepartmentGrade(department, 1)).thenReturn(
                    listOf(
                        2150999901L,
                        2150118601L,
                        2150118602L,
                        2150888801L,
                    )
                )

                // 의도적으로 섞어서 반환 (정렬 검증)
                whenever(
                    courseReader.findAllInCategory(
                        category = eq(Category.TEACHING),
                        courseCodes = any(),
                        schoolId = eq(26),
                    )
                ).thenReturn(
                    listOf(
                        teachingCourse(
                            code = 2150118602L,
                            name = "교육학개론",
                            field = "교직영역/교직이론",
                            professor = "B",
                        ),
                        teachingCourse(
                            code = 2150999901L,
                            name = "교과교육론",
                            field = "전공영역/교과교육영역",
                        ),
                        teachingCourse(
                            code = 2150118601L,
                            name = "교육학개론",
                            field = "교직영역/교직이론",
                            professor = "A",
                        ),
                        teachingCourse(
                            code = 2150888801L,
                            name = "특성화세미나",
                            field = "특성화/특성화",
                        ),
                    )
                )

                val result = service.recommend(ctx(teaching = true))

                result.category shouldBe "TEACHING"
                result.message shouldBe null
                result.courses shouldHaveSize 3

                // 영역 순서: 전공영역 -> 교직영역 -> 특성화영역
                result.courses.map { it.courseName } shouldContainExactly listOf(
                    "교과교육론",
                    "교육학개론",
                    "특성화세미나",
                )
                result.courses.map { it.field } shouldContainExactly listOf(
                    "전공영역",
                    "교직영역",
                    "특성화영역",
                )

                // 같은 baseCode(교육학개론 21501186)의 분반이 한 과목으로 그룹핑
                result.courses[1].sections shouldHaveSize 2
                // 교직 추천은 timing을 사용하지 않음
                result.courses[1].timing shouldBe null
                // 교직 추천은 department를 포함하지 않음
                result.courses[1].department shouldBe null

                verify(courseReader, times(1)).findAllInCategory(
                    category = eq(Category.TEACHING),
                    courseCodes = any(),
                    schoolId = eq(26),
                )
                verify(targetReader, times(5)).findAllByDepartmentGrade(eq(department), any())
                verify(targetReader, times(5)).findAllByDepartmentGradeForTeaching(eq(department), any())
            }
        }
    }
})
