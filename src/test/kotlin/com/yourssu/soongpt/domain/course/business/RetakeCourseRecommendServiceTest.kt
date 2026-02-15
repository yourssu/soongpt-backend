package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.business.dto.Progress
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
                result.progress.required shouldBe -2
                result.progress.completed shouldBe -2
                result.progress.satisfied shouldBe false
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
                result.progress.required shouldBe -1
                result.progress.completed shouldBe -1
                result.courses shouldHaveSize 0
                result.message shouldBe "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
            }
        }
    }

    /**
     * 교양필수 재수강 매핑 시나리오:
     * Python(rusaint_service)에서 "독서와토론"(구과목, baseCode 99990001) 감지 →
     * 대체 신과목 baseCode "21501003"을 lowGradeSubjectCodes에 추가.
     * Kotlin은 [구과목코드(매칭 없음), 신과목baseCode(매칭 있음)] 두 개를 받아서
     * 신과목 "[인문적상상력과소통]고전읽기와상상력"을 재수강 추천 결과에 포함해야 한다.
     */
    given("교양필수 재수강 매핑: 구과목(독서와토론) + 대체 신과목 baseCode가 포함된 경우") {
        // 구과목 코드 99990001 → 현재 DB에 없음 (폐강)
        // 신과목 baseCode 21501003 → [인문적상상력과소통]고전읽기와상상력 (현재 개설)
        val replacementSection1 = CourseWithTarget(
            course = Course(
                id = 100L,
                category = Category.GENERAL_REQUIRED,
                code = 2150100314L,
                name = "[인문적상상력과소통]고전읽기와상상력",
                professor = "박준웅",
                department = "교양교육운영팀",
                division = null,
                time = "2.0",
                point = "2",
                personeel = 30,
                scheduleRoom = "토 09:00-09:50 (진리관 11305-박준웅)\n토 10:00-10:50 (진리관 11305-박준웅)",
                target = "전체학년 미디어경영",
                credit = 2.0,
            ),
            targetGrades = listOf(1, 2, 3, 4),
            isStrict = false,
        )
        val replacementSection2 = CourseWithTarget(
            course = Course(
                id = 101L,
                category = Category.GENERAL_REQUIRED,
                code = 2150100315L,
                name = "[인문적상상력과소통]고전읽기와상상력",
                professor = "김영희",
                department = "교양교육운영팀",
                division = null,
                time = "2.0",
                point = "2",
                personeel = 30,
                scheduleRoom = "토 11:00-11:50 (진리관 11305-김영희)\n토 12:00-12:50 (진리관 11305-김영희)",
                target = "전체학년 미디어경영",
                credit = 2.0,
            ),
            targetGrades = listOf(1, 2, 3, 4),
            isStrict = false,
        )

        // 구과목 코드 99990001 → DB에 없으므로 baseCodes=[99990001, 21501003]으로 조회 시
        // 21501003에 해당하는 과목만 반환
        whenever(
            courseRepository.findCoursesWithTargetByBaseCodes(listOf(99990001L, 21501003L))
        ).thenReturn(listOf(replacementSection1, replacementSection2))

        `when`("recommend를 호출하면") {
            // Python에서 추가된 대체 baseCode가 lowGradeSubjectCodes에 포함된 상태
            val result = service.recommend(listOf("99990001", "21501003"))

            then("대체 신과목이 재수강 추천에 포함된다") {
                result.category shouldBe "RETAKE"
                result.courses shouldHaveSize 1
                result.courses[0].courseName shouldBe "[인문적상상력과소통]고전읽기와상상력"
                result.courses[0].baseCourseCode shouldBe 21501003L
                result.courses[0].credits shouldBe 2.0
                result.courses[0].department shouldBe "교양교육운영팀"
                result.message.shouldBeNull()
            }

            then("대체 신과목의 분반이 올바르게 반환된다") {
                result.courses[0].sections shouldHaveSize 2
                result.courses[0].professors shouldBe listOf("김영희", "박준웅")
            }
        }
    }

    given("교양필수 재수강 매핑: 현대인과성서 구과목 + 대체 신과목이 함께 있는 경우") {
        val replacementCourse = CourseWithTarget(
            course = Course(
                id = 200L,
                category = Category.GENERAL_REQUIRED,
                code = 2150102019L,
                name = "[인간과성서]현대사회이슈와기독교",
                professor = "설충수",
                department = "교양교육운영팀",
                division = null,
                time = "2.0",
                point = "2",
                personeel = 30,
                scheduleRoom = "토 09:00-09:50 (진리관 11306-설충수)\n토 10:00-10:50 (진리관 11306-설충수)",
                target = "전체학년 미디어경영",
                credit = 2.0,
            ),
            targetGrades = listOf(1, 2, 3, 4),
            isStrict = false,
        )

        whenever(
            courseRepository.findCoursesWithTargetByBaseCodes(listOf(88880001L, 21501020L))
        ).thenReturn(listOf(replacementCourse))

        `when`("recommend를 호출하면") {
            val result = service.recommend(listOf("88880001", "21501020"))

            then("현대사회이슈와기독교가 재수강 추천에 포함된다") {
                result.courses shouldHaveSize 1
                result.courses[0].courseName shouldBe "[인간과성서]현대사회이슈와기독교"
                result.courses[0].baseCourseCode shouldBe 21501020L
            }
        }
    }

    /**
     * Kotlin 하드코딩(RETAKE_OLD_TO_REPLACEMENT): 구과목 baseCode만 lowGradeSubjectCodes에 있어도
     * 치환된 대체 신과목으로 조회해 재수강 추천에 포함된다.
     */
    listOf(
        Triple("독서와토론", "21506685", 21501003L to "[인문적상상력과소통]고전읽기와상상력"),
        Triple("현대인과성서", "21503037", 21501020L to "[인간과성서]현대사회이슈와기독교"),
        Triple("기업가정신과행동", "21500898", 21501009L to "[창의적사고와혁신]혁신과기업가정신"),
        Triple("대학글쓰기", "21500901", 21501006L to "[비판적사고와표현]미디어사회와비평적글쓰기"),
        Triple("컴퓨터사고", "21500907", 21501028L to "[컴퓨팅적사고]컴퓨팅적사고와코딩기초"),
        Triple("AI와데이터사회", "21500747", 21501034L to "[SW와AI]AI와데이터기초"),
    ).forEach { (구과목명, 구과목코드, replacement) ->
        val (대체BaseCode, 대체과목명) = replacement
        given("교양필수 재수강 하드코딩: 구과목 $구과목명 ($구과목코드)만 lowGradeSubjectCodes에 있는 경우") {
            val replacementCourse = CourseWithTarget(
                course = Course(
                    id = 300L,
                    category = Category.GENERAL_REQUIRED,
                    code = 대체BaseCode * 100 + 1,
                    name = 대체과목명,
                    professor = "테스트교수",
                    department = "교양교육운영팀",
                    division = null,
                    time = "2.0",
                    point = "2",
                    personeel = 30,
                    scheduleRoom = "월 09:00-09:50",
                    target = "전체학년",
                    credit = 2.0,
                ),
                targetGrades = listOf(1, 2, 3, 4),
                isStrict = false,
            )
            whenever(
                courseRepository.findCoursesWithTargetByBaseCodes(listOf(구과목코드.toLong(), 대체BaseCode))
            ).thenReturn(listOf(replacementCourse))

            `when`("recommend를 호출하면") {
                val result = service.recommend(listOf(구과목코드))

                then("치환된 대체 신과목이 재수강 추천에 포함된다") {
                    result.category shouldBe "RETAKE"
                    result.courses shouldHaveSize 1
                    result.courses[0].courseName shouldBe 대체과목명
                    result.courses[0].baseCourseCode shouldBe 대체BaseCode
                    result.message.shouldBeNull()
                }
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

            then("RETAKE 카테고리, notApplicable progress, 과목 목록을 반환한다") {
                result.category shouldBe "RETAKE"
                result.progress.required shouldBe -1
                result.progress.completed shouldBe -1
                result.progress.satisfied shouldBe false
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
