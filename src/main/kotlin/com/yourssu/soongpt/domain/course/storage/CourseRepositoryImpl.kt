package com.yourssu.soongpt.domain.course.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component

@Component
class CourseRepositoryImpl(
    private val courseJpaRepository: CourseJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
    ): CourseRepository {
    override fun get(code: Long): Course {
        return courseJpaRepository.getByCode(code).toDomain()
    }

    override fun getAll(codes: List<Long>): List<Course> {
        return courseJpaRepository.getAllByCode(codes)
            .map { it.toDomain() }
    }

    override fun getAllInCategory(category: Category, courseIds: List<Long>): List<Course> {
        return jpaQueryFactory.
            selectFrom(courseEntity)
            .where(
                courseEntity.category.eq(category),
                courseEntity.code.`in`(courseIds)
            )
            .fetch()
            .map { it.toDomain() }
    }

    override fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        val categories = listOf(Category.MAJOR_REQUIRED, Category.MAJOR_ELECTIVE, Category.GENERAL_REQUIRED,
            Category.GENERAL_ELECTIVE)
        val groupedCourses = categories.associateWith { category ->
            getAll(codes).filter { it.category == category }
        }
        return GroupedCoursesByCategoryDto.from(
            majorRequiredCourses = groupedCourses[Category.MAJOR_REQUIRED]?: emptyList(),
            majorElectiveCourses = groupedCourses[Category.MAJOR_ELECTIVE]?: emptyList(),
            generalRequiredCourses = groupedCourses[Category.GENERAL_REQUIRED]?: emptyList(),
            generalElectiveCourses = groupedCourses[Category.GENERAL_ELECTIVE]?: emptyList(),
        )
    }
}

interface CourseJpaRepository: JpaRepository<CourseEntity, Long> {
    fun getByCode(code: Long): CourseEntity

    @Query("select c from CourseEntity c where c.code in :codes")
    fun getAllByCode(codes: List<Long>): List<CourseEntity>
}
