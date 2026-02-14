package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.Progress
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GeneralCourseRecommendServiceTest : BehaviorSpec({

    val courseRepository = mock<CourseRepository>()
    val departmentReader = mock<DepartmentReader>()
    val courseFieldReader = mock<CourseFieldReader>()
    val courseService = mock<CourseService>()
    val service = GeneralCourseRecommendService(courseRepository, departmentReader, courseFieldReader, courseService)

    val department = Department(id = 1L, name = "컴퓨터학부", collegeId = 10L)
    val departmentName = "컴퓨터학부"
    val userGrade = 3
    val schoolId = 23

    beforeSpec {
        whenever(departmentReader.getByName(departmentName)).thenReturn(department)
    }

    given("교양필수 - progress가 satisfied일 때") {
        val progress = Progress(required = 12, completed = 12, satisfied = true)

        `when`("recommend를 호출하면") {
            val result = service.recommend(
                category = Category.GENERAL_REQUIRED,
                departmentName = departmentName,
                userGrade = userGrade,
                schoolId = schoolId,
                admissionYear = 2023,
                takenSubjectCodes = emptyList(),
                progress = progress,
            )

            then("satisfied 메시지를 반환한다") {
                result.category shouldBe "GENERAL_REQUIRED"
                result.progress shouldBe progress
                result.message shouldBe "교양필수 학점을 이미 모두 이수하셨습니다."
                result.courses shouldHaveSize 0
                result.lateFields.shouldBeNull()
            }
        }
    }

    given("교양필수 - 개설 과목이 없을 때") {
        val progress = Progress(required = 12, completed = 6, satisfied = false)

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_REQUIRED,
                    departmentId = 1L,
                    collegeId = 10L,
                    userGrade = userGrade,
                    maxGrade = userGrade,
                )
            ).thenReturn(emptyList())
        }

        `when`("recommend를 호출하면") {
            val result = service.recommend(
                category = Category.GENERAL_REQUIRED,
                departmentName = departmentName,
                userGrade = userGrade,
                schoolId = schoolId,
                admissionYear = 2023,
                takenSubjectCodes = emptyList(),
                progress = progress,
            )

            then("빈 과목 메시지를 반환한다") {
                result.category shouldBe "GENERAL_REQUIRED"
                result.message shouldBe "이번 학기에 수강 가능한 교양필수 과목이 없습니다."
                result.courses shouldHaveSize 0
            }
        }
    }

    given("교양필수 - LATE 분야와 ON_TIME 분야가 모두 있을 때") {
        val progress = Progress(required = 12, completed = 3, satisfied = false)

        // SW와AI (1학년 대상) → 3학년 사용자에게 LATE
        val swCourse1 = CourseWithTarget(
            course = Course(
                id = 1L,
                category = Category.GENERAL_REQUIRED,
                code = 1110000101L,
                name = "SW기초",
                professor = "김교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 200,
                scheduleRoom = "월 09:00-10:15 (전산관 101)",
                target = "전체 1학년",
                credit = 3.0,
                field = "['23이후]SW와AI\n['20~'22]생활속의SW",
            ),
            targetGrades = listOf(1),
            isStrict = false,
        )

        // 창의적사고와혁신 (3학년 권장 분야) → 3학년 사용자에게 ON_TIME
        val creativeCourse1 = CourseWithTarget(
            course = Course(
                id = 2L,
                category = Category.GENERAL_REQUIRED,
                code = 1110000201L,
                name = "창의혁신프로젝트",
                professor = "박교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 150,
                scheduleRoom = "화 13:30-14:45 (인문관 201)",
                target = "전체 3학년",
                credit = 3.0,
                field = "['23이후]창의적사고와혁신\n['20~'22]창의융합",
            ),
            targetGrades = listOf(3),
            isStrict = false,
        )
        val creativeCourse2 = CourseWithTarget(
            course = Course(
                id = 3L,
                category = Category.GENERAL_REQUIRED,
                code = 1110000202L,
                name = "창의혁신프로젝트",
                professor = "최교수",
                department = "교양교육원",
                division = "02분반",
                time = "3.0",
                point = "3",
                personeel = 150,
                scheduleRoom = "수 10:00-11:15 (인문관 202)",
                target = "전체 3학년",
                credit = 3.0,
                field = "['23이후]창의적사고와혁신\n['20~'22]창의융합",
            ),
            targetGrades = listOf(3),
            isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_REQUIRED,
                    departmentId = 1L,
                    collegeId = 10L,
                    userGrade = userGrade,
                    maxGrade = userGrade,
                )
            ).thenReturn(listOf(swCourse1, creativeCourse1, creativeCourse2))
        }

        `when`("recommend를 호출하면") {
            val result = service.recommend(
                category = Category.GENERAL_REQUIRED,
                departmentName = departmentName,
                userGrade = userGrade,
                schoolId = schoolId,
                admissionYear = 2023,
                takenSubjectCodes = emptyList(),
                progress = progress,
            )

            then("LATE 분야는 lateFields에 텍스트로 포함된다") {
                result.lateFields.shouldNotBeNull()
                result.lateFields!! shouldHaveSize 1
                result.lateFields!![0] shouldBe "SW와AI"
            }

            then("ON_TIME 분야는 courses에 field와 함께 포함된다") {
                result.courses shouldHaveSize 1
                result.courses[0].field shouldBe "창의적사고와혁신"
                result.courses[0].courseName shouldBe "창의혁신프로젝트"
                result.courses[0].sections shouldHaveSize 2
            }
        }
    }

    given("교양필수 - 이미 수강한 분야는 전체 제외") {
        val progress = Progress(required = 12, completed = 6, satisfied = false)

        // SW와AI 분야의 과목 (baseCode = 11100001)
        val swCourse = CourseWithTarget(
            course = Course(
                id = 1L,
                category = Category.GENERAL_REQUIRED,
                code = 1110000101L,
                name = "SW기초",
                professor = "김교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 200,
                scheduleRoom = "월 09:00-10:15 (전산관 101)",
                target = "전체 1학년",
                credit = 3.0,
                field = "['23이후]SW와AI",
            ),
            targetGrades = listOf(1),
            isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_REQUIRED,
                    departmentId = 1L,
                    collegeId = 10L,
                    userGrade = userGrade,
                    maxGrade = userGrade,
                )
            ).thenReturn(listOf(swCourse))

            // takenFields DB lookup: baseCode 11100001 → SW와AI 분야의 swCourse 반환
            whenever(
                courseRepository.findCoursesWithTargetByBaseCodes(any())
            ).thenReturn(listOf(swCourse))
        }

        `when`("해당 분야의 과목을 이미 수강했으면") {
            val result = service.recommend(
                category = Category.GENERAL_REQUIRED,
                departmentName = departmentName,
                userGrade = userGrade,
                schoolId = schoolId,
                admissionYear = 2023,
                takenSubjectCodes = listOf("11100001"), // SW기초의 baseCode
                progress = progress,
            )

            then("분야 전체가 제외되어 빈 결과를 반환한다") {
                result.message shouldBe "이번 학기에 수강 가능한 교양필수 과목이 없습니다."
                result.courses shouldHaveSize 0
                result.lateFields.shouldBeNull()
            }
        }
    }

    given("교양필수 - 22학번 이하: lateFields 없음, 과목 단위만 제외") {
        val progress = Progress(required = 12, completed = 3, satisfied = false)

        val swCourse1 = CourseWithTarget(
            course = Course(
                id = 1L,
                category = Category.GENERAL_REQUIRED,
                code = 1110000101L,
                name = "SW기초",
                professor = "김교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 200,
                scheduleRoom = "월 09:00-10:15",
                target = "전체 1학년",
                credit = 3.0,
                field = "['23이후]SW와AI",
            ),
            targetGrades = listOf(1),
            isStrict = false,
        )
        val swCourse2 = CourseWithTarget(
            course = Course(
                id = 2L,
                category = Category.GENERAL_REQUIRED,
                code = 1110000201L,
                name = "AI기초",
                professor = "이교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 200,
                scheduleRoom = "수 09:00-10:15",
                target = "전체 1학년",
                credit = 3.0,
                field = "['23이후]SW와AI",
            ),
            targetGrades = listOf(1),
            isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_REQUIRED,
                    departmentId = 1L,
                    collegeId = 10L,
                    userGrade = userGrade,
                    maxGrade = userGrade,
                )
            ).thenReturn(listOf(swCourse1, swCourse2))
        }

        `when`("22학번이고 한 과목만 수강했으면") {
            val result = service.recommend(
                category = Category.GENERAL_REQUIRED,
                departmentName = departmentName,
                userGrade = userGrade,
                schoolId = 22,
                admissionYear = 2022,
                takenSubjectCodes = listOf("11100001"), // SW기초만 이수
                progress = progress,
            )

            then("lateFields는 null이다") {
                result.lateFields.shouldBeNull()
            }
            then("수강한 과목만 제외하고 같은 분야 다른 과목은 포함된다") {
                result.courses shouldHaveSize 1
                result.courses[0].courseName shouldBe "AI기초"
                result.courses[0].field shouldBe "SW와AI"
            }
        }
    }

    given("교양선택 - 분야별 그룹핑") {
        val progress = Progress(required = 15, completed = 3, satisfied = false)

        val course1 = CourseWithTarget(
            course = Course(
                id = 10L,
                category = Category.GENERAL_ELECTIVE,
                code = 2220000101L,
                name = "영화와사회",
                professor = "이교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 100,
                scheduleRoom = "목 15:00-16:15 (인문관 301)",
                target = "전체",
                credit = 3.0,
                field = "['23이후]문화와예술",
            ),
            targetGrades = listOf(1, 2, 3, 4),
            isStrict = false,
        )

        val course2 = CourseWithTarget(
            course = Course(
                id = 11L,
                category = Category.GENERAL_ELECTIVE,
                code = 2220000201L,
                name = "경제학원론",
                professor = "정교수",
                department = "교양교육원",
                division = "01분반",
                time = "3.0",
                point = "3",
                personeel = 120,
                scheduleRoom = "금 10:00-11:15 (경상관 201)",
                target = "전체",
                credit = 3.0,
                field = "['23이후]사회와경제",
            ),
            targetGrades = listOf(1, 2, 3, 4),
            isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_ELECTIVE,
                    departmentId = 1L,
                    collegeId = 10L,
                    userGrade = userGrade,
                    maxGrade = userGrade,
                )
            ).thenReturn(listOf(course1, course2))
        }

        `when`("recommend를 호출하면") {
            val result = service.recommend(
                category = Category.GENERAL_ELECTIVE,
                departmentName = departmentName,
                userGrade = userGrade,
                schoolId = schoolId,
                admissionYear = 2023,
                takenSubjectCodes = emptyList(),
                progress = progress,
            )

            then("분야별 과목이 courses에 field와 함께 포함된다") {
                result.category shouldBe "GENERAL_ELECTIVE"
                result.courses shouldHaveSize 2
                result.courses.map { it.field }.toSet() shouldBe setOf("문화와예술", "사회와경제")
            }

            then("lateFields는 null이다 (교선은 LATE 구분 없음)") {
                result.lateFields.shouldBeNull()
            }
        }
    }
})
