package com.yourssu.soongpt.domain.target.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.department.storage.QDepartmentEntity.departmentEntity
import com.yourssu.soongpt.domain.departmentGrade.storage.QDepartmentGradeEntity.departmentGradeEntity
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class TargetRepositoryImpl(
    private val targetJpaRepository: TargetJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
): TargetRepository {
    override fun save(target: Target): Target {
        return targetJpaRepository.save(TargetEntity.from(target))
            .toDomain()
    }

    override fun findAllByCourseId(courseId: Long): List<Target> {
        return jpaQueryFactory.selectFrom(targetEntity)
            .where(targetEntity.courseId.eq(courseId))
            .fetch()
            .map { it.toDomain() }
    }

    override fun findAllByCourseIdToDisplayName(courseId: Long): List<String> {
        return jpaQueryFactory.select(departmentEntity.name, departmentGradeEntity.grade)
            .from(targetEntity)
            .innerJoin(departmentGradeEntity)
            .on(targetEntity.departmentGradeId.eq(departmentGradeEntity.id))
            .innerJoin(departmentEntity)
            .on(departmentGradeEntity.departmentId.eq(departmentEntity.id))
            .where(targetEntity.courseId.eq(courseId))
            .fetch()
            .map { "${it.get(departmentEntity.name)}${it.get(departmentGradeEntity.grade)}" }

    }
}

interface TargetJpaRepository: JpaRepository<TargetEntity, Long> {
}