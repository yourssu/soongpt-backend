package com.yourssu.soongpt.domain.course.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
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

    override fun searchCourses(
        query: String,
        pageable: Pageable
    ): Page<Course> {
        val whereCondition = courseEntity.field.containsIgnoreCase(query)
            .or(courseEntity.code.stringValue().containsIgnoreCase(query))
            .or(courseEntity.name.containsIgnoreCase(query))
            .or(courseEntity.professor.containsIgnoreCase(query))
            .or(courseEntity.scheduleRoom.containsIgnoreCase(query))
            .or(courseEntity.target.containsIgnoreCase(query))
        
        // 전체 개수 조회
        val totalCount = jpaQueryFactory
            .select(courseEntity.count())
            .from(courseEntity)
            .where(whereCondition)
            .fetchOne() ?: 0L
        
        // 페이징된 결과 조회
        val content = jpaQueryFactory
            .selectFrom(courseEntity)
            .where(whereCondition)
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(
                courseEntity.name.startsWithIgnoreCase(query).desc(),
                courseEntity.name.asc()
            )
            .fetch()
            .map { it.toDomain() }
        
        return PageImpl(content, pageable, totalCount)
    }
}

interface CourseJpaRepository: JpaRepository<CourseEntity, Long> {
    fun getByCode(code: Long): CourseEntity

    @Query("select c from CourseEntity c where c.code in :codes")
    fun getAllByCode(codes: List<Long>): List<CourseEntity>
}
