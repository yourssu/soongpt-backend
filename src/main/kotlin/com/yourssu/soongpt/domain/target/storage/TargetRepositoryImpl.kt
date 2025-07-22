package com.yourssu.soongpt.domain.target.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class TargetRepositoryImpl (
    val targetJpaRepository: TargetJpaRepository,
    val jpaQueryFactory: JPAQueryFactory
): TargetRepository {
    override fun findAllByCourseId(courseId: Long): List<Target> {
        return jpaQueryFactory
            .selectFrom(targetEntity)
            .where(targetEntity.courseId .eq(courseId))
            .fetch()
            .map { it.toDomain() }
    }

    override fun findAllByDepartmentGrade(
        departmentId: Long,
        grade: Int
    ): List<Target> {
        return jpaQueryFactory
            .selectFrom(targetEntity)
            .where(
                targetEntity.departmentId.eq(departmentId),
                targetEntity.grade.eq(grade)
            )
            .fetch()
            .map { it.toDomain() }
    }
}

interface TargetJpaRepository : JpaRepository<TargetEntity, Long> {
}
