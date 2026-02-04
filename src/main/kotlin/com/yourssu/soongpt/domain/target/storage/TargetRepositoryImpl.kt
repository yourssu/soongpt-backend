package com.yourssu.soongpt.domain.target.storage

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType
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
        return jpaQueryFactory
            .selectFrom(targetEntity)
            .where(targetEntity.courseCode.eq(code))
            .fetch()
            .map { it.toDomain() }
    }

    override fun findAllByDepartmentGrade(
        departmentId: Long,
        collegeId: Long,
        grade: Int
    ): List<Long> {
        val gradeCondition = when (grade) {
            1 -> targetEntity.grade1.isTrue
            2 -> targetEntity.grade2.isTrue
            3 -> targetEntity.grade3.isTrue
            4 -> targetEntity.grade4.isTrue
            5 -> targetEntity.grade5.isTrue
            else -> throw IllegalArgumentException("Invalid grade: $grade")
        }

        val scopeCondition = targetEntity.scopeType.eq(ScopeType.UNIVERSITY)
            .or(
                targetEntity.scopeType.eq(ScopeType.COLLEGE)
                    .and(targetEntity.collegeId.eq(collegeId))
            )
            .or(
                targetEntity.scopeType.eq(ScopeType.DEPARTMENT)
                    .and(targetEntity.departmentId.eq(departmentId))
            )

        val allowCourses = jpaQueryFactory
            .select(targetEntity.courseCode)
            .from(targetEntity)
            .where(
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isFalse,
                gradeCondition,
                scopeCondition
            )
            .fetch()
            .toSet()

        if (allowCourses.isEmpty()) {
            return emptyList()
        }

        val denyCourses = jpaQueryFactory
            .select(targetEntity.courseCode)
            .from(targetEntity)
            .where(
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isTrue,
                gradeCondition,
                scopeCondition
            )
            .fetch()
            .toSet()

        return (allowCourses - denyCourses).toList()
    }

    override fun findAllByClass(departmentId: Long, code: Long, grade: Int): List<Target> {
        val codeWithoutDivision = code.div(DIVISION_DIVISOR)
        val gradeCondition = when (grade) {
            1 -> targetEntity.grade1.isTrue
            2 -> targetEntity.grade2.isTrue
            3 -> targetEntity.grade3.isTrue
            4 -> targetEntity.grade4.isTrue
            5 -> targetEntity.grade5.isTrue
            else -> null
        }

        return jpaQueryFactory
            .selectFrom(targetEntity)
            .innerJoin(courseEntity)
            .on(targetEntity.courseCode.eq(courseEntity.code))
            .where(
                targetEntity.departmentId.eq(departmentId),
                courseEntity.code.divide(DIVISION_DIVISOR).longValue().eq(codeWithoutDivision),
                gradeCondition
            )
            .fetch()
            .map { it.toDomain() }
    }

    override fun findAllByDepartmentGradeRange(
        departmentId: Long,
        collegeId: Long,
        maxGrade: Int
    ): List<Long> {
        val gradeCondition = buildGradeRangeCondition(maxGrade)

        val scopeCondition = targetEntity.scopeType.eq(ScopeType.UNIVERSITY)
            .or(
                targetEntity.scopeType.eq(ScopeType.COLLEGE)
                    .and(targetEntity.collegeId.eq(collegeId))
            )
            .or(
                targetEntity.scopeType.eq(ScopeType.DEPARTMENT)
                    .and(targetEntity.departmentId.eq(departmentId))
            )

        val allowCourses = jpaQueryFactory
            .select(targetEntity.courseCode)
            .from(targetEntity)
            .where(
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isFalse,
                gradeCondition,
                scopeCondition
            )
            .fetch()
            .toSet()

        if (allowCourses.isEmpty()) {
            return emptyList()
        }

        val denyCourses = jpaQueryFactory
            .select(targetEntity.courseCode)
            .from(targetEntity)
            .where(
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isTrue,
                gradeCondition,
                scopeCondition
            )
            .fetch()
            .toSet()

        return (allowCourses - denyCourses).toList()
    }

    private fun buildGradeRangeCondition(maxGrade: Int): BooleanExpression {
        var condition = targetEntity.grade1.isTrue
        if (maxGrade >= 2) condition = condition.or(targetEntity.grade2.isTrue)
        if (maxGrade >= 3) condition = condition.or(targetEntity.grade3.isTrue)
        if (maxGrade >= 4) condition = condition.or(targetEntity.grade4.isTrue)
        if (maxGrade >= 5) condition = condition.or(targetEntity.grade5.isTrue)
        return condition
    }
}

interface TargetJpaRepository : JpaRepository<TargetEntity, Long> {
}
