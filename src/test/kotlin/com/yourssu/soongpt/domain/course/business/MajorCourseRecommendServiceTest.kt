package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.business.dto.CourseTiming
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class MajorCourseRecommendServiceTest : BehaviorSpec({

    val courseRepository = mock<CourseRepository>()
    val departmentReader = mock<DepartmentReader>()

    val service = MajorCourseRecommendService(
        courseRepository = courseRepository,
        departmentReader = departmentReader,
    )

    val department = Department(id = 1L, name = "컴퓨터학부", collegeId = 10L)

    fun stubDepartment() {
        whenever(departmentReader.getByName("컴퓨터학부")).thenReturn(department)
    }

    fun course(
        code: Long,
        name: String,
        category: Category,
        professor: String? = null,
        target: String = "전체",
    ): Course {
        return Course(
            id = null,
            category = category,
            code = code,
            name = name,
            professor = professor,
            department = "컴퓨터학부",
            division = null,
            time = "3.0",
            point = "3",
            personeel = 30,
            scheduleRoom = "월 09:00-10:15 (정보과학관)",
            target = target,
            credit = 3.0,
        )
    }

    beforeTest {
        reset(courseRepository, departmentReader)
        stubDepartment()
    }

    given("전공기초/전공필수 추천") {
        `when`("progress가 satisfied이면") {
            then("DB 조회 없이 satisfied 응답을 반환한다") {
                val result = service.recommendMajorBasicOrRequired(
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    category = Category.MAJOR_BASIC,
                    takenSubjectCodes = emptyList(),
                    progress = Progress(required = 6, completed = 6, satisfied = true),
                )

                result.category shouldBe "MAJOR_BASIC"
                result.courses shouldHaveSize 0
                result.message shouldBe "전공기초 학점을 이미 모두 이수하셨습니다."

                verify(courseRepository, never()).findCoursesWithTargetByCategory(
                    category = any(),
                    departmentId = any(),
                    collegeId = any(),
                    maxGrade = any(),
                )
            }
        }

        `when`("미충족 상태에서 미이수 과목이 있으면") {
            then("takenSubjectCodes(baseCode) 필터 후 LATE 우선 정렬로 추천한다") {
                val taken = CourseWithTarget(
                    course = course(
                        code = 2150545501L,
                        name = "데이터구조",
                        category = Category.MAJOR_BASIC,
                        professor = "홍길동",
                        target = "2학년",
                    ),
                    targetGrades = listOf(2),
                )
                val late = CourseWithTarget(
                    course = course(
                        code = 2150545601L,
                        name = "알고리즘",
                        category = Category.MAJOR_BASIC,
                        professor = "김교수",
                        target = "1학년",
                    ),
                    targetGrades = listOf(1), // userGrade=3 -> LATE
                )
                val onTime = CourseWithTarget(
                    course = course(
                        code = 2150545701L,
                        name = "운영체제",
                        category = Category.MAJOR_BASIC,
                        professor = "박교수",
                        target = "3학년",
                    ),
                    targetGrades = listOf(3),
                )

                whenever(
                    courseRepository.findCoursesWithTargetByCategory(
                        category = Category.MAJOR_BASIC,
                        departmentId = 1L,
                        collegeId = 10L,
                        maxGrade = 3,
                    )
                ).thenReturn(listOf(taken, late, onTime))

                val result = service.recommendMajorBasicOrRequired(
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    category = Category.MAJOR_BASIC,
                    takenSubjectCodes = listOf("2150545501"), // 데이터구조 이수
                    progress = Progress(required = 6, completed = 0, satisfied = false),
                )

                // taken 제외 → 2개 남음
                result.courses shouldHaveSize 2
                // LATE 우선
                result.courses.map { it.courseName } shouldContainExactly listOf("알고리즘", "운영체제")
                result.courses[0].timing shouldBe CourseTiming.LATE
                result.courses[1].timing shouldBe CourseTiming.ON_TIME
            }
        }
    }

    given("전공선택 추천(통합 엔드포인트용)") {
        `when`("전선 과목과 타전공인정 과목이 모두 있으면") {
            then("전선 먼저, 타전공인정은 뒤에 isCrossMajor=true로 포함된다") {
                val progress = Progress(required = 54, completed = 0, satisfied = false)

                val elective = CourseWithTarget(
                    course = course(
                        code = 2150100101L,
                        name = "컴퓨터네트워크",
                        category = Category.MAJOR_ELECTIVE,
                        professor = "A",
                        target = "3학년",
                    ),
                    targetGrades = listOf(3),
                )

                val crossMajor = CourseWithTarget(
                    course = course(
                        code = 2150200201L,
                        name = "디지털신호처리",
                        category = Category.MAJOR_ELECTIVE,
                        professor = "B",
                        target = "2학년",
                    ),
                    targetGrades = listOf(2),
                )

                whenever(
                    courseRepository.findCoursesWithTargetByCategory(
                        category = Category.MAJOR_ELECTIVE,
                        departmentId = 1L,
                        collegeId = 10L,
                        maxGrade = 5,
                    )
                ).thenReturn(listOf(elective))

                whenever(
                    courseRepository.findCoursesWithTargetBySecondaryMajor(
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = SecondaryMajorCompletionType.RECOGNIZED,
                        departmentId = 1L,
                        collegeId = 10L,
                        maxGrade = 5,
                    )
                ).thenReturn(listOf(crossMajor))

                val result = service.recommendMajorElectiveWithGroups(
                    departmentName = "컴퓨터학부",
                    userGrade = 3,
                    takenSubjectCodes = emptyList(),
                    progress = progress,
                )

                result.category shouldBe "MAJOR_ELECTIVE"
                result.message shouldBe null
                result.userGrade shouldBe 3

                result.courses shouldHaveSize 2
                result.courses.map { it.courseName } shouldContainExactly listOf("컴퓨터네트워크", "디지털신호처리")
                result.courses[0].isCrossMajor shouldBe false
                result.courses[1].isCrossMajor shouldBe true

                verify(courseRepository, times(1)).findCoursesWithTargetBySecondaryMajor(
                    trackType = eq(SecondaryMajorTrackType.CROSS_MAJOR),
                    completionType = eq(SecondaryMajorCompletionType.RECOGNIZED),
                    departmentId = eq(1L),
                    collegeId = eq(10L),
                    maxGrade = eq(5),
                )
            }
        }
    }
})
