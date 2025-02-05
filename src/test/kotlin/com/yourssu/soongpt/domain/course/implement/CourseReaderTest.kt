package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.CourseFixture.MAJOR_REQUIRED
import com.yourssu.soongpt.common.support.fixture.DepartmentFixture.COMPUTER
import com.yourssu.soongpt.common.support.fixture.DepartmentGradeFixture.FIRST
import com.yourssu.soongpt.common.support.fixture.TargetFixture
import com.yourssu.soongpt.domain.course.implement.exception.CourseNotFoundException
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeRepository
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class CourseReaderTest {
    @Autowired
    private lateinit var courseReader: CourseReader

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var departmentRepository: DepartmentRepository

    @Autowired
    private lateinit var departmentGradeRepository: DepartmentGradeRepository

    @Autowired
    private lateinit var targetRepository: TargetRepository


    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findAllByCourseNameInMajorRequired_메서드는 {
        var departmentId: Long? = null
        var courseName: String? = null

        @BeforeEach
        fun setUp() {
            val course = courseRepository.save(MAJOR_REQUIRED.toDomainRandomCourseCode())

            courseName = course.courseName
            val department = departmentRepository.save(COMPUTER.toDomain(1L))
            departmentId = department.id
            val departmentGrade = departmentGradeRepository.save(FIRST.toDomain(departmentId = departmentId!!))
            targetRepository.save(
                TargetFixture.TARGET1.toDomain(
                    departmentGradeId = departmentGrade.id!!,
                    courseId = course.id!!
                )
            )
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 학과이름이_일치하지_않는_경우 {
            @Test
            @DisplayName("CourseNotFoundException 예외를 반환한다.")
            fun success() {
                assertThrows<CourseNotFoundException> {
                    courseReader.findAllByCourseNameInMajorRequired(
                        departmentId = departmentId!!,
                        courseName = "일치하지 않는 과목 이름",
                    )
                }
            }
        }

        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 수강대상이_일치하지_않는_경우 {
            @Test
            @DisplayName("CourseNotFoundException 예외를 반환한다.")
            fun success() {
                assertThrows<CourseNotFoundException> {
                    courseReader.findAllByCourseNameInMajorRequired(
                        departmentId = 0L,
                        courseName = courseName!!,
                    )
                }
            }
        }
    }
}