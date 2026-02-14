package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.college.implement.College
import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.course.business.query.FilterCoursesByTrackQuery
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.CourseWithTarget
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CourseServiceImplByTrackTest : BehaviorSpec({
    val courseReader = mock<CourseReader>()
    val departmentReader = mock<DepartmentReader>()
    val targetReader = mock<TargetReader>()
    val collegeReader = mock<CollegeReader>()
    val courseFieldReader = mock<CourseFieldReader>()

    val service = CourseServiceImpl(
        courseReader = courseReader,
        departmentReader = departmentReader,
        targetReader = targetReader,
        collegeReader = collegeReader,
        courseFieldReader = courseFieldReader,
    )

    val department = Department(id = 8L, name = "컴퓨터학부", collegeId = 2L)
    val college = College(id = 2L, name = "IT대학")

    fun stubBaseLookups() {
        whenever(departmentReader.getByName("컴퓨터학부")).thenReturn(department)
        whenever(collegeReader.get(2L)).thenReturn(college)
    }

    beforeTest {
        reset(courseReader, departmentReader, targetReader, collegeReader, courseFieldReader)
        stubBaseLookups()
    }

    fun sampleCourse(code: Long, name: String): Course {
        return Course(
            category = Category.MAJOR_ELECTIVE,
            code = code,
            name = name,
            department = "전자정보공학부",
            time = "",
            point = "3",
            scheduleRoom = "월 09:00-10:15 (강의실)",
            target = "3학년",
        )
    }

    given("CROSS_MAJOR 조회") {
        `when`("completionType이 지정되면 target 필터 없이 분류 테이블 기준으로 조회한다") {
            then("분류 조회 메서드만 사용한다") {
                whenever(
                    courseReader.findCoursesBySecondaryMajorClassification(
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = SecondaryMajorCompletionType.RECOGNIZED,
                        departmentId = 8L,
                    )
                ).thenReturn(
                    listOf(
                        sampleCourse(2150014701, "디지털신호처리"),
                        sampleCourse(2150014702, "디지털신호처리"),
                    )
                )

                val result = service.findAllByTrack(
                    FilterCoursesByTrackQuery(
                        schoolId = 26,
                        departmentName = "컴퓨터학부",
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = SecondaryMajorCompletionType.RECOGNIZED,
                    )
                )

                result shouldHaveSize 2
                verify(courseReader, times(1)).findCoursesBySecondaryMajorClassification(
                    trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                    completionType = SecondaryMajorCompletionType.RECOGNIZED,
                    departmentId = 8L,
                )
                verify(courseReader, never()).findCoursesWithTargetBySecondaryMajor(
                    trackType = eq(SecondaryMajorTrackType.CROSS_MAJOR),
                    completionType = any(),
                    departmentId = any(),
                    collegeId = any(),
                    userGrade = any(),
                    maxGrade = any(),
                )
            }
        }

        `when`("컴퓨터학부 타전공인정 과목이 target 조건과 무관하게 분류에만 존재해도 반환한다") {
            then("컴퓨터학부(8) 기준 분류 결과를 그대로 반환한다") {
                whenever(
                    courseReader.findCoursesBySecondaryMajorClassification(
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = SecondaryMajorCompletionType.RECOGNIZED,
                        departmentId = 8L,
                    )
                ).thenReturn(
                    listOf(
                        sampleCourse(2150014701, "디지털신호처리"),
                        sampleCourse(2150123401, "확률과통계"),
                    )
                )

                val result = service.findAllByTrack(
                    FilterCoursesByTrackQuery(
                        schoolId = 26,
                        departmentName = "컴퓨터학부",
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = SecondaryMajorCompletionType.RECOGNIZED,
                    )
                )

                result.map { it.code } shouldContainExactly listOf(2150014701L, 2150123401L)
                verify(courseReader, never()).findCoursesWithTargetBySecondaryMajor(
                    trackType = eq(SecondaryMajorTrackType.CROSS_MAJOR),
                    completionType = any(),
                    departmentId = any(),
                    collegeId = any(),
                    userGrade = any(),
                    maxGrade = any(),
                )
            }
        }

        `when`("completionType이 없으면 RECOGNIZED로 조회한다") {
            then("RECOGNIZED 1회 조회 결과를 반환한다") {
                whenever(
                    courseReader.findCoursesBySecondaryMajorClassification(
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = SecondaryMajorCompletionType.RECOGNIZED,
                        departmentId = 8L,
                    )
                ).thenReturn(listOf(sampleCourse(2150014701, "디지털신호처리")))

                val result = service.findAllByTrack(
                    FilterCoursesByTrackQuery(
                        schoolId = 26,
                        departmentName = "컴퓨터학부",
                        trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                        completionType = null,
                    )
                )

                result shouldHaveSize 1
                verify(courseReader, times(1)).findCoursesBySecondaryMajorClassification(
                    trackType = SecondaryMajorTrackType.CROSS_MAJOR,
                    completionType = SecondaryMajorCompletionType.RECOGNIZED,
                    departmentId = 8L,
                )
            }
        }
    }

    given("DOUBLE_MAJOR 조회") {
        `when`("기존 target 기반 로직을 유지한다") {
            then("학년 루프(1~5) 기반 target 조회를 수행한다") {
                val targetCourses =
                    listOf(
                        CourseWithTarget(
                            course = sampleCourse(2150014701, "디지털신호처리"),
                            targetGrades = listOf(3),
                        )
                    )

                (1..5).forEach { grade ->
                    whenever(
                        courseReader.findCoursesWithTargetBySecondaryMajor(
                            trackType = SecondaryMajorTrackType.DOUBLE_MAJOR,
                            completionType = SecondaryMajorCompletionType.REQUIRED,
                            departmentId = 8L,
                            collegeId = 2L,
                            userGrade = grade,
                            maxGrade = grade,
                        )
                    ).thenReturn(targetCourses)
                }

                val result = service.findAllByTrack(
                    FilterCoursesByTrackQuery(
                        schoolId = 26,
                        departmentName = "컴퓨터학부",
                        trackType = SecondaryMajorTrackType.DOUBLE_MAJOR,
                        completionType = SecondaryMajorCompletionType.REQUIRED,
                    )
                )

                result shouldHaveSize 1
                verify(courseReader, times(5)).findCoursesWithTargetBySecondaryMajor(
                    trackType = eq(SecondaryMajorTrackType.DOUBLE_MAJOR),
                    completionType = eq(SecondaryMajorCompletionType.REQUIRED),
                    departmentId = eq(8L),
                    collegeId = eq(2L),
                    userGrade = any(),
                    maxGrade = any(),
                )
                verify(courseReader, never()).findCoursesBySecondaryMajorClassification(
                    trackType = eq(SecondaryMajorTrackType.DOUBLE_MAJOR),
                    completionType = any(),
                    departmentId = any(),
                )
            }
        }
    }
})
