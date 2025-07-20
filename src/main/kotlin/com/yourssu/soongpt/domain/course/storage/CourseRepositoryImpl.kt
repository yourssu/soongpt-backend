package com.yourssu.soongpt.domain.course.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.target.implement.Target
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

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

    override fun findAllByCategoryTarget(
        category: Category,
        target: Target
    ): List<Course> {
        return listOf()
    }
}

interface CourseJpaRepository: JpaRepository<CourseEntity, Long> {
    fun getByCode(code: Long): CourseEntity

    @Query("select c from CourseEntity c where c.code in :codes")
    fun getAllByCode(codes: List<Long>): List<CourseEntity>
}
