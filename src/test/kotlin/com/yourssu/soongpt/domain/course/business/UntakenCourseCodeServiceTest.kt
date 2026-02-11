package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintBasicInfoDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintStudentFlagsDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintTakenCourseDto
import com.yourssu.soongpt.domain.usaint.implement.dto.RusaintUsaintDataResponse
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class UntakenCourseCodeServiceTest : BehaviorSpec({

    val courseRepository = mock<CourseRepository>()
    val departmentReader = mock<DepartmentReader>()
    val syncSessionStore = mock<SyncSessionStore>()
    val service = UntakenCourseCodeService(courseRepository, departmentReader, syncSessionStore)

    val department = Department(id = 1L, name = "컴퓨터학부", collegeId = 10L)
    val pseudonym = "test-pseudonym"

    val usaintData = RusaintUsaintDataResponse(
        pseudonym = pseudonym,
        takenCourses = listOf(
            RusaintTakenCourseDto(year = 2023, semester = "1", subjectCodes = listOf("11100001", "22200002")),
        ),
        lowGradeSubjectCodes = emptyList(),
        flags = RusaintStudentFlagsDto(doubleMajorDepartment = null, minorDepartment = null, teaching = false),
        basicInfo = RusaintBasicInfoDto(year = 2023, semester = 1, grade = 3, department = "컴퓨터학부"),
    )

    beforeSpec {
        CurrentPseudonymHolder.set(pseudonym)
        whenever(syncSessionStore.getUsaintData(pseudonym)).thenReturn(usaintData)
        whenever(departmentReader.getByName("컴퓨터학부")).thenReturn(department)
    }

    afterSpec {
        CurrentPseudonymHolder.clear()
    }

    given("전기 - 미수강 과목코드 조회") {
        val taken = CourseWithTarget(
            course = Course(
                id = 1L, category = Category.MAJOR_BASIC, code = 1110000101L,
                name = "프로그래밍기초", professor = "김교수", department = "컴퓨터학부",
                division = "01분반", time = "3.0", point = "3", personeel = 50,
                scheduleRoom = "월 09:00-10:15", target = "1학년", credit = 3.0,
            ),
            targetGrades = listOf(1), isStrict = false,
        )
        val untaken = CourseWithTarget(
            course = Course(
                id = 2L, category = Category.MAJOR_BASIC, code = 3330000301L,
                name = "자료구조", professor = "박교수", department = "컴퓨터학부",
                division = "01분반", time = "3.0", point = "3", personeel = 50,
                scheduleRoom = "화 10:00-11:15", target = "2학년", credit = 3.0,
            ),
            targetGrades = listOf(2), isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.MAJOR_BASIC, departmentId = 1L, collegeId = 10L, maxGrade = 3,
                )
            ).thenReturn(listOf(taken, untaken))
        }

        `when`("getUntakenCourseCodes를 호출하면") {
            val result = service.getUntakenCourseCodes(Category.MAJOR_BASIC)

            then("이수한 과목(baseCode 11100001)은 제외하고 미수강 과목코드만 반환한다") {
                result shouldHaveSize 1
                result[0] shouldBe 3330000301L
            }
        }
    }

    given("전선 - 전학년 과목 조회") {
        val course1 = CourseWithTarget(
            course = Course(
                id = 3L, category = Category.MAJOR_ELECTIVE, code = 4440000401L,
                name = "인공지능", professor = "이교수", department = "컴퓨터학부",
                division = "01분반", time = "3.0", point = "3", personeel = 40,
                scheduleRoom = "수 13:00-14:15", target = "4학년", credit = 3.0,
            ),
            targetGrades = listOf(4), isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.MAJOR_ELECTIVE, departmentId = 1L, collegeId = 10L, maxGrade = 5,
                )
            ).thenReturn(listOf(course1))
        }

        `when`("getUntakenCourseCodes를 호출하면") {
            val result = service.getUntakenCourseCodes(Category.MAJOR_ELECTIVE)

            then("maxGrade=5로 전학년 과목을 조회한다") {
                result shouldHaveSize 1
                result[0] shouldBe 4440000401L
            }
        }
    }

    given("교필 - 분야별 미수강 과목코드 조회") {
        val swCourse = CourseWithTarget(
            course = Course(
                id = 10L, category = Category.GENERAL_REQUIRED, code = 1110000101L,
                name = "SW기초", professor = "김교수", department = "교양교육원",
                division = "01분반", time = "3.0", point = "3", personeel = 200,
                scheduleRoom = "월 09:00-10:15", target = "1학년", credit = 3.0,
                field = "['23이후]SW와AI",
            ),
            targetGrades = listOf(1), isStrict = false,
        )
        val globalCourse = CourseWithTarget(
            course = Course(
                id = 11L, category = Category.GENERAL_REQUIRED, code = 5550000501L,
                name = "세계시민론", professor = "박교수", department = "교양교육원",
                division = "01분반", time = "3.0", point = "3", personeel = 150,
                scheduleRoom = "화 13:30-14:45", target = "3학년", credit = 3.0,
                field = "['23이후]글로벌시민의식",
            ),
            targetGrades = listOf(3), isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_REQUIRED, departmentId = 1L, collegeId = 10L, maxGrade = 3,
                )
            ).thenReturn(listOf(swCourse, globalCourse))
        }

        `when`("SW와AI 분야를 이미 이수한 경우") {
            val result = service.getUntakenCourseCodesByField(Category.GENERAL_REQUIRED)

            then("SW와AI 분야는 빈 리스트, 글로벌시민의식 분야는 과목코드를 반환한다") {
                result shouldHaveSize 2
                result shouldContainKey "SW와AI"
                result["SW와AI"]!! shouldHaveSize 0
                result shouldContainKey "글로벌시민의식"
                result["글로벌시민의식"]!! shouldContainExactlyInAnyOrder listOf(5550000501L)
            }
        }
    }

    given("교선 - baseCode 단위 개별 제외") {
        val course1 = CourseWithTarget(
            course = Course(
                id = 20L, category = Category.GENERAL_ELECTIVE, code = 1110000101L,
                name = "영화와사회", professor = "이교수", department = "교양교육원",
                division = "01분반", time = "3.0", point = "3", personeel = 100,
                scheduleRoom = "목 15:00-16:15", target = "전체", credit = 3.0,
                field = "['23이후]문화와예술",
            ),
            targetGrades = listOf(1, 2, 3, 4), isStrict = false,
        )
        val course2 = CourseWithTarget(
            course = Course(
                id = 21L, category = Category.GENERAL_ELECTIVE, code = 6660000601L,
                name = "음악감상", professor = "최교수", department = "교양교육원",
                division = "01분반", time = "3.0", point = "3", personeel = 80,
                scheduleRoom = "금 10:00-11:15", target = "전체", credit = 3.0,
                field = "['23이후]문화와예술",
            ),
            targetGrades = listOf(1, 2, 3, 4), isStrict = false,
        )

        beforeContainer {
            whenever(
                courseRepository.findCoursesWithTargetByCategory(
                    category = Category.GENERAL_ELECTIVE, departmentId = 1L, collegeId = 10L, maxGrade = 3,
                )
            ).thenReturn(listOf(course1, course2))
        }

        `when`("영화와사회(baseCode=11100001)만 이수한 경우") {
            val result = service.getUntakenCourseCodesByField(Category.GENERAL_ELECTIVE)

            then("같은 분야의 다른 과목(음악감상)은 남아있다") {
                result shouldContainKey "문화와예술"
                result["문화와예술"]!! shouldContainExactlyInAnyOrder listOf(6660000601L)
            }
        }
    }
})
