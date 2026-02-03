package com.yourssu.soongpt.domain.course.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component

private const val DIVISION_DIVISOR = 100

@Component
class CourseRepositoryImpl(
    private val courseJpaRepository: CourseJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
    ): CourseRepository {
    private val totalCount: Long = courseJpaRepository.count()

    override fun get(code: Long): Course {
        return courseJpaRepository.getByCode(code).toDomain()
    }

    @Cacheable(value = ["courseCache"], key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    override fun findAll(pageable: Pageable): Page<Course> {
        val content = jpaQueryFactory
            .selectFrom(courseEntity)
            .orderBy(courseEntity.name.asc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
            .map { it.toDomain() }
        return PageImpl(content, pageable, totalCount)
    }

    override fun findAllById(courseIds: List<Long>): List<Course> {
        return courseJpaRepository.findAllById(courseIds)
            .map { it.toDomain() }
    }

    override fun findAllByCode(codes: List<Long>): List<Course> {
        return courseJpaRepository.getAllByCode(codes)
            .map { it.toDomain() }
    }

    override fun findAllInCategory(category: Category, courseCodes: List<Long>): List<Course> {
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(
                courseEntity.code.`in`(courseCodes),
                courseEntity.category.eq(category),
            )
            .fetch()
            .map { it.toDomain() }
    }

    override fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto {
        val groupedCourses = findAllByCode(codes).groupBy { it.category }
        return GroupedCoursesByCategoryDto.from(groupedCourses)
    }

    override fun searchCourses(
        query: String,
        pageable: Pageable
    ): Page<Course> {
        val content = jpaQueryFactory
            .selectFrom(courseEntity)
            .where(buildSearchCondition(query))
            .orderBy(
                courseEntity.name.lower().indexOf(query.lowercase()).asc(),
                courseEntity.name.length().asc(),
                courseEntity.name.asc()
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
            .map { it.toDomain() }
        return PageImpl(content, pageable, countCoursesByQuery(query))
    }

    override fun findAllByClass(code: Long): List<Course> {
        val codeWithoutDivision = code.div(DIVISION_DIVISOR)
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(courseEntity.code.divide(DIVISION_DIVISOR).longValue().eq(codeWithoutDivision))
            .fetch()
            .map { it.toDomain() }
    }

    private fun buildSearchCondition(query: String) =
        courseEntity.name.containsIgnoreCase(query)
            .or(courseEntity.professor.containsIgnoreCase(query))

    private fun countCoursesByQuery(query: String): Long {
        return jpaQueryFactory
            .select(courseEntity.count())
            .from(courseEntity)
            .where(buildSearchCondition(query))
            .fetchOne() ?: 0L
    }
}

interface CourseJpaRepository: JpaRepository<CourseEntity, Long> {
    @Query("select c from CourseEntity c where c.code = :code")
    fun getByCode(code: Long): CourseEntity

    @Query("select c from CourseEntity c where c.code in :codes")
    fun getAllByCode(codes: List<Long>): List<CourseEntity>
}
