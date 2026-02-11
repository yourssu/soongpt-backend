package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RetakeCourseRecommendServiceTest : BehaviorSpec({

    val courseRepository = mock<CourseRepository>()
    val service = RetakeCourseRecommendService(courseRepository)

    given("lowGradeSubjectCodes가 비어있을 때") {
        `when`("recommend를 호출하면") {
            val result = service.recommend(emptyList())

            then("RETAKE 카테고리와 빈 과목 목록, 안내 메시지를 반환한다") {
                result.category shouldBe "RETAKE"
                result.progress.shouldBeNull()
                result.courses shouldHaveSize 0
                result.message shouldBe "재수강 가능한 C+ 이하 과목이 없습니다."
            }
        }
    }

    given("lowGradeSubjectCodes가 있지만 매칭 과목이 없을 때") {
        whenever(courseRepository.findCoursesWithTargetByBaseCodes(listOf(21505455L))).thenReturn(emptyList())

        `when`("recommend를 호출하면") {
            val result = service.recommend(listOf("21505455"))

            then("RETAKE 카테고리와 빈 과목 목록, 개설 없음 메시지를 반환한다") {
                result.category shouldBe "RETAKE"
                result.progress.shouldBeNull()
                result.courses shouldHaveSize 0
                result.message shouldBe "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
            }
        }
    }

    given("매칭 과목이 있을 때") {
        val course1Section1 = CourseWithTarget(
            course = Course(
                id = 1L,
                category = Category.MAJOR_REQUIRED,
                code = 2150545501L,
                name = "데이터구조",
                professor = "홍길동",
                department = "컴퓨터학부",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 40,
                scheduleRoom = "화 09:00-10:15 (정보과학관 21001)",
                target = "컴퓨터학부 2학년",
                credit = 3.0,
            ),
            targetGrades = listOf(2),
            isStrict = true,
        )
        val course1Section2 = CourseWithTarget(
            course = Course(
                id = 2L,
                category = Category.MAJOR_REQUIRED,
                code = 2150545502L,
                name = "데이터구조",
                professor = "김철수",
                department = "컴퓨터학부",
                division = "02분반",
                time = "3.0",
                point = "3",
                personeel = 40,
                scheduleRoom = "목 13:30-14:45 (정보과학관 21002)",
                target = "컴퓨터학부 2학년",
                credit = 3.0,
            ),
            targetGrades = listOf(2),
            isStrict = false,
        )
        val course2Section1 = CourseWithTarget(
            course = Course(
                id = 3L,
                category = Category.MAJOR_ELECTIVE,
                code = 2150545601L,
                name = "알고리즘",
                professor = "이영희",
                department = "컴퓨터학부",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 35,
                scheduleRoom = "월 10:30-11:45 (정보과학관 21003)",
                target = "컴퓨터학부 전체",
                credit = 3.0,
            ),
            targetGrades = listOf(1, 2, 3, 4),
            isStrict = false,
        )

        whenever(
            courseRepository.findCoursesWithTargetByBaseCodes(listOf(21505455L, 21505456L))
        ).thenReturn(listOf(course1Section1, course1Section2, course2Section1))

        `when`("recommend를 호출하면") {
            val result = service.recommend(listOf("21505455", "21505456"))

            then("RETAKE 카테고리, null progress, 과목 목록을 반환한다") {
                result.category shouldBe "RETAKE"
                result.progress.shouldBeNull()
                result.message.shouldBeNull()
            }

            then("과목이 courseName 기준으로 정렬된다") {
                result.courses shouldHaveSize 2
                result.courses[0].courseName shouldBe "데이터구조"
                result.courses[1].courseName shouldBe "알고리즘"
            }

            then("timing은 null이다") {
                result.courses[0].timing.shouldBeNull()
                result.courses[1].timing.shouldBeNull()
            }

            then("target은 Course.target 문자열 그대로다") {
                result.courses[0].target shouldBe "컴퓨터학부 2학년"
                result.courses[1].target shouldBe "컴퓨터학부 전체"
            }

            then("professors가 중복 제거되고 정렬된다") {
                result.courses[0].professors shouldBe listOf("김철수", "홍길동")
            }

            then("분반이 올바르게 그룹핑된다") {
                result.courses[0].sections shouldHaveSize 2
                result.courses[1].sections shouldHaveSize 1
            }

            then("isStrictRestriction이 분반별로 매핑된다") {
                result.courses[0].sections[0].isStrictRestriction shouldBe true
                result.courses[0].sections[1].isStrictRestriction shouldBe false
            }

            then("department가 항상 포함된다") {
                result.courses[0].department shouldBe "컴퓨터학부"
                result.courses[1].department shouldBe "컴퓨터학부"
            }

            then("schedule은 강의실 제외한 요일/시간 포맷이다") {
                result.courses[0].sections[0].schedule shouldBe "화 09:00-10:15"
            }

            then("division은 과목코드 마지막 2자리이다") {
                result.courses[0].sections[0].division shouldBe "01"
                result.courses[0].sections[1].division shouldBe "02"
            }
        }
    }
})
