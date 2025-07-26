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

    override fun findAllById(courseIds: List<Long>): List<Course> {
        return courseJpaRepository.findAllById(courseIds)
            .map { it.toDomain() }
    }

    override fun findAllByCode(codes: List<Long>): List<Course> {
        return courseJpaRepository.getAllByCode(codes)
            .map { it.toDomain() }
    }

    override fun findAllInCategory(category: Category, courseIds: List<Long>): List<Course> {
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(
                courseEntity.id.`in`(courseIds),
                courseEntity.category.eq(category),
            )
            .fetch()
            .map { it.toDomain() }
    }

    override fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        val groupedCourses = findAllByCode(codes).groupBy { it.category }
        return GroupedCoursesByCategoryDto.from(groupedCourses)
    }
}

interface CourseJpaRepository: JpaRepository<CourseEntity, Long> {
    fun getByCode(code: Long): CourseEntity

    @Query("select c from CourseEntity c where c.code in :codes")
    fun getAllByCode(codes: List<Long>): List<CourseEntity>
}
