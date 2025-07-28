package com.yourssu.soongpt.domain.target.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

private const val DIVISION_DIVISOR = 100

@Component
class TargetRepositoryImpl (
    val targetJpaRepository: TargetJpaRepository,
    val jpaQueryFactory: JPAQueryFactory,
): TargetRepository {
    override fun findAllByCode(code: Long): List<Target> {
        return listOf()
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

    override fun findAllByClass(departmentId: Long, code: Long, grade: Int): List<Target> {
        val codeWithoutDivision = code.div(DIVISION_DIVISOR)
        return jpaQueryFactory
            .selectFrom(targetEntity)
            .innerJoin(courseEntity)
            .on(targetEntity.courseId.eq(courseEntity.id))
            .where(
                targetEntity.departmentId.eq(departmentId),
                courseEntity.code.divide(DIVISION_DIVISOR).longValue().eq(codeWithoutDivision),
                targetEntity.grade.eq(grade)
            )
            .fetch()
            .map { it.toDomain() }
    }
}

interface TargetJpaRepository : JpaRepository<TargetEntity, Long> {
}
