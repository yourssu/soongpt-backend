package com.yourssu.soongpt.domain.target.implement

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.department.implement.Department
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TargetReaderTest : BehaviorSpec({

    val targetRepository = mock<TargetRepository>()
    val targetReader = TargetReader(targetRepository)

    val department = Department(id = 1L, name = "컴퓨터학부", collegeId = 10L)

    beforeTest {
        reset(targetRepository)
    }

    given("TargetReader.findCourseCodesByCategory") {
        `when`("전공기초/전공필수는 1~현재학년 범위로 과목 코드를 조회한다") {
            then("중복을 제거하여 반환하고, 4~5학년 조회는 수행하지 않는다") {
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 1))
                    .thenReturn(listOf(1001L, 1002L))
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 2))
                    .thenReturn(listOf(1002L, 1003L))
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 3))
                    .thenReturn(listOf(1003L, 1004L))

                val result = targetReader.findCourseCodesByCategory(
                    department = department,
                    userGrade = 3,
                    category = Category.MAJOR_BASIC,
                )

                result shouldContainExactly listOf(1001L, 1002L, 1003L, 1004L)

                // 1~3학년 조회
                verify(targetRepository, times(1)).findAllByDepartmentGrade(1L, 10L, 1)
                verify(targetRepository, times(1)).findAllByDepartmentGrade(1L, 10L, 2)
                verify(targetRepository, times(1)).findAllByDepartmentGrade(1L, 10L, 3)

                // 4~5학년은 조회하지 않음
                verify(targetRepository, never()).findAllByDepartmentGrade(1L, 10L, 4)
                verify(targetRepository, never()).findAllByDepartmentGrade(1L, 10L, 5)
            }
        }

        `when`("전공선택은 전학년(1~5) 범위로 과목 코드를 조회한다") {
            then("1~5학년 조회 결과를 합쳐 중복 제거 후 반환한다") {
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 1))
                    .thenReturn(listOf(2001L))
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 2))
                    .thenReturn(listOf(2001L))
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 3))
                    .thenReturn(emptyList())
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 4))
                    .thenReturn(listOf(2002L))
                whenever(targetRepository.findAllByDepartmentGrade(1L, 10L, 5))
                    .thenReturn(listOf(2002L))

                val result = targetReader.findCourseCodesByCategory(
                    department = department,
                    userGrade = 3,
                    category = Category.MAJOR_ELECTIVE,
                )

                result shouldContainExactly listOf(2001L, 2002L)

                (1..5).forEach { grade ->
                    verify(targetRepository, times(1)).findAllByDepartmentGrade(1L, 10L, grade)
                }
            }
        }

        `when`("지원하지 않는 카테고리면") {
            then("빈 리스트를 반환하고 Repository 조회를 수행하지 않는다") {
                val result = targetReader.findCourseCodesByCategory(
                    department = department,
                    userGrade = 3,
                    category = Category.GENERAL_REQUIRED,
                )

                result.shouldBeEmpty()
                verifyNoInteractions(targetRepository)
            }
        }
    }
})
