package com.yourssu.soongpt.domain.course.business

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.common.business.initialization.CollegesAndDepartmentsInitializer
import com.yourssu.soongpt.common.support.config.ApplicationTest
import com.yourssu.soongpt.common.support.fixture.CourseFixture
import com.yourssu.soongpt.common.support.fixture.CourseTimeFixture
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeRepository
import com.yourssu.soongpt.domain.department.storage.QDepartmentEntity.departmentEntity
import com.yourssu.soongpt.domain.departmentGrade.storage.QDepartmentGradeEntity.departmentGradeEntity
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.springframework.beans.factory.annotation.Autowired

@ApplicationTest
class CourseServiceTest {
    @Autowired
    private lateinit var courseService: CourseService

    @Autowired
    private lateinit var courseRepository: CourseRepository

    @Autowired
    private lateinit var targetRepository: TargetRepository

    @Autowired
    private lateinit var courseTimeRepository: CourseTimeRepository

    @Autowired
    private lateinit var jpaQueryFactory: JPAQueryFactory

    @Autowired
    private lateinit var initializer: CollegesAndDepartmentsInitializer

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findByDepartmentNameInMajorRequired_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 학과를_받으면 {
            val departmentName = "소프트웨어학부"
            @BeforeEach
            fun setUp() {
                initializer.run()
                val course = courseRepository.save(CourseFixture.MAJOR_REQUIRED.toDomain())
                val departmentGrade = jpaQueryFactory.selectFrom(departmentGradeEntity)
                    .innerJoin(departmentEntity)
                    .on(departmentGradeEntity.departmentId.eq(departmentEntity.id))
                    .where(departmentEntity.name.eq(departmentName), departmentGradeEntity.grade.eq(4))
                    .fetchOne()
                    ?.toDomain()
                    ?: throw IllegalArgumentException("소프트웨어학부 4학년이 존재하지 않습니다.")
                targetRepository.save(Target(departmentGradeId = departmentGrade.id!!, courseId = course.id!!))
                courseTimeRepository.save(CourseTimeFixture.MONDAY_17_19.toDomain(course.id!!))
            }

            @Test
            @DisplayName("해당 학과가 수강대상인 과목 정보를 반환한다.")
            fun success() {
                val response = courseService.findByDepartmentNameInMajorRequired(FoundDepartmentCommand(departmentName, 4))

                assertEquals(1, response.size)
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findByDepartmentNameInMajorElective_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 학과를_받으면 {
            val departmentName = "소프트웨어학부"
            @BeforeEach
            fun setUp() {
                initializer.run()
                val course = courseRepository.save(CourseFixture.MAJOR_ELECTIVE.toDomain())
                val departmentGrade = jpaQueryFactory.selectFrom(departmentGradeEntity)
                    .innerJoin(departmentEntity)
                    .on(departmentGradeEntity.departmentId.eq(departmentEntity.id))
                    .where(departmentEntity.name.eq(departmentName), departmentGradeEntity.grade.eq(4))
                    .fetchOne()
                    ?.toDomain()
                    ?: throw IllegalArgumentException("소프트웨어학부 4학년이 존재하지 않습니다.")
                targetRepository.save(Target(departmentGradeId = departmentGrade.id!!, courseId = course.id!!))
                courseTimeRepository.save(CourseTimeFixture.MONDAY_17_19.toDomain(course.id!!))
            }

            @Test
            @DisplayName("해당 학과가 수강대상인 과목 정보를 반환한다.")
            fun success() {
                val response = courseService.findByDepartmentNameInMajorElective(FoundDepartmentCommand(departmentName, 4))

                assertEquals(1, response.size)
            }
        }
    }

    @Nested
    @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
    inner class findByDepartmentNameInGeneralRequired_메서드는 {
        @Nested
        @DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores::class)
        inner class 학과를_받으면 {
            val departmentName = "소프트웨어학부"
            @BeforeEach
            fun setUp() {
                initializer.run()
                val course = courseRepository.save(CourseFixture.GENERAL_REQUIRED.toDomain())
                val departmentGrade = jpaQueryFactory.selectFrom(departmentGradeEntity)
                    .innerJoin(departmentEntity)
                    .on(departmentGradeEntity.departmentId.eq(departmentEntity.id))
                    .where(departmentEntity.name.eq(departmentName), departmentGradeEntity.grade.eq(4))
                    .fetchOne()
                    ?.toDomain()
                    ?: throw IllegalArgumentException("소프트웨어학부 4학년이 존재하지 않습니다.")
                targetRepository.save(Target(departmentGradeId = departmentGrade.id!!, courseId = course.id!!))
                courseTimeRepository.save(CourseTimeFixture.MONDAY_17_19.toDomain(course.id!!))
            }

            @Test
            @DisplayName("해당 학과가 수강대상인 과목 정보를 반환한다.")
            fun success() {
                val response = courseService.findByDepartmentNameInGeneralRequired(FoundDepartmentCommand(departmentName, 4))

                assertEquals(1, response.size)
            }
        }
    }
}