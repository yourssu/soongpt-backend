package com.yourssu.soongpt.domain.courseTime.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeRepository
import com.yourssu.soongpt.domain.courseTime.storage.QCourseTimeEntity.courseTimeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class CourseTimeRepositoryImpl(
    private val courseTimeJpaRepository: CourseTimeJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : CourseTimeRepository {
    override fun save(courseTime: CourseTime): CourseTime {
        return courseTimeJpaRepository.save(CourseTimeEntity.from(courseTime))
            .toDomain()
    }

    override fun findAllByCourseId(courseId: Long): List<CourseTime> {
        return jpaQueryFactory.selectFrom(courseTimeEntity)
            .where(courseTimeEntity.courseId.eq(courseId))
            .fetch()
            .map { it.toDomain() }
    }
}

interface CourseTimeJpaRepository : JpaRepository<CourseTimeEntity, Long> {
}
