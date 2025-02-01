package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.CourseFixture.MAJOR_REQUIRED
import com.yourssu.soongpt.common.support.fixture.DepartmentFixture.COMPUTER
import com.yourssu.soongpt.common.support.fixture.DepartmentGradeFixture.FIRST
import com.yourssu.soongpt.common.support.fixture.TargetFixture
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.department.implement.DepartmentRepository
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeRepository
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.Test

@ApplicationTest
class CourseRepositoryImplTest {
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
    inner class findAllByDepartmentId_메서드는 {
        var departmentId: Long? = null

        @BeforeEach
        fun setUp() {
            val course = courseRepository.save(MAJOR_REQUIRED.toDomainRandomCourseCode())
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
        inner class 학과_아이디와_이수구분을_받으면 {
            @Test
            @DisplayName("해당하는 과목을 반환한다.")
            fun success() {
                val courses = courseRepository.findAllByDepartmentId(
                    departmentId = departmentId!!,
                    classification = MAJOR_REQUIRED.classification
                )

                assertThat(courses).hasSize(1)
            }
        }
    }
}