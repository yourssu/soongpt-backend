package com.yourssu.soongpt.domain.target.storage

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.DIVISION_DIVISOR
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetRepository
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class TargetRepositoryImpl(
        val targetJpaRepository: TargetJpaRepository,
        val jpaQueryFactory: JPAQueryFactory,
) : TargetRepository {

    override fun findAllByCode(code: Long): List<Target> {
        return jpaQueryFactory
                .selectFrom(targetEntity)
                .where(targetEntity.courseCode.eq(code))
                .fetch()
                .map { it.toDomain() }
    }

    override fun findAllByCodes(codes: List<Long>): List<Target> {
        if (codes.isEmpty()) return emptyList()
        return jpaQueryFactory
                .selectFrom(targetEntity)
                .where(targetEntity.courseCode.`in`(codes))
                .fetch()
                .map { it.toDomain() }
    }

    override fun findAllByDepartmentGrade(
            departmentId: Long,
            collegeId: Long,
            grade: Int
    ): List<Long> {
        val gradeCondition =
                buildGradeCondition(grade)
                        ?: throw IllegalArgumentException("Invalid grade: $grade")
        return findCourseCodesByCondition(departmentId, collegeId, gradeCondition, StudentType.GENERAL)
    }

    override fun findAllByDepartmentGradeForTeaching(
            departmentId: Long,
            collegeId: Long,
            grade: Int
    ): List<Long> {
        val gradeCondition =
                buildGradeCondition(grade)
                        ?: throw IllegalArgumentException("Invalid grade: $grade")
        return findCourseCodesByCondition(departmentId, collegeId, gradeCondition, StudentType.TEACHING_CERT)
    }

    override fun findAllByDepartmentGradeRange(
            departmentId: Long,
            collegeId: Long,
            maxGrade: Int
    ): List<Long> {
        val gradeCondition = buildGradeRangeCondition(maxGrade)
        return findCourseCodesByCondition(departmentId, collegeId, gradeCondition, StudentType.GENERAL)
    }

    override fun findAllByClass(departmentId: Long, code: Long, grade: Int): List<Target> {
        val codeWithoutDivision = code.div(DIVISION_DIVISOR)
        val gradeCondition = buildGradeCondition(grade)

        return jpaQueryFactory
                .selectFrom(targetEntity)
                .innerJoin(courseEntity)
                .on(targetEntity.courseCode.eq(courseEntity.code))
                .where(
                        targetEntity.departmentId.eq(departmentId),
                        courseEntity
                                .code
                                .divide(DIVISION_DIVISOR)
                                .longValue()
                                .eq(codeWithoutDivision),
                        gradeCondition
                )
                .fetch()
                .map { it.toDomain() }
    }

    // ============ Private Helper Methods ============

    /** 공통 과목 코드 조회 로직 (Allow - Deny) */
    private fun findCourseCodesByCondition(
            departmentId: Long,
            collegeId: Long,
            gradeCondition: BooleanExpression,
            studentType: StudentType = StudentType.GENERAL
    ): List<Long> {
        val scopeCondition = buildScopeCondition(departmentId, collegeId)

        val allowCourses =
                jpaQueryFactory
                        .select(targetEntity.courseCode)
                        .from(targetEntity)
                        .where(
                                targetEntity.studentType.eq(studentType),
                                targetEntity.isDenied.isFalse,
                                gradeCondition,
                                scopeCondition
                        )
                        .fetch()
                        .toSet()

        if (allowCourses.isEmpty()) {
            return emptyList()
        }

        val denyCourses =
                jpaQueryFactory
                        .select(targetEntity.courseCode)
                        .from(targetEntity)
                        .where(
                                targetEntity.studentType.eq(studentType),
                                targetEntity.isDenied.isTrue,
                                gradeCondition,
                                scopeCondition
                        )
                        .fetch()
                        .toSet()

        return (allowCourses - denyCourses).toList()
    }

    /** 범위 조건 (UNIVERSITY / COLLEGE / DEPARTMENT) */
    private fun buildScopeCondition(departmentId: Long, collegeId: Long): BooleanExpression {
        return targetEntity
                .scopeType
                .eq(ScopeType.UNIVERSITY)
                .or(
                        targetEntity
                                .scopeType
                                .eq(ScopeType.COLLEGE)
                                .and(targetEntity.collegeId.eq(collegeId))
                )
                .or(
                        targetEntity
                                .scopeType
                                .eq(ScopeType.DEPARTMENT)
                                .and(targetEntity.departmentId.eq(departmentId))
                )
    }

    /** 단일 학년 조건 */
    private fun buildGradeCondition(grade: Int): BooleanExpression? {
        return when (grade) {
            1 -> targetEntity.grade1.isTrue
            2 -> targetEntity.grade2.isTrue
            3 -> targetEntity.grade3.isTrue
            4 -> targetEntity.grade4.isTrue
            5 -> targetEntity.grade5.isTrue
            else -> null
        }
    }

    /** 학년 범위 조건 (1 ~ maxGrade) */
    private fun buildGradeRangeCondition(maxGrade: Int): BooleanExpression {
        var condition = targetEntity.grade1.isTrue
        if (maxGrade >= 2) condition = condition.or(targetEntity.grade2.isTrue)
        if (maxGrade >= 3) condition = condition.or(targetEntity.grade3.isTrue)
        if (maxGrade >= 4) condition = condition.or(targetEntity.grade4.isTrue)
        if (maxGrade >= 5) condition = condition.or(targetEntity.grade5.isTrue)
        return condition
    }

    override fun saveAll(targets: List<Target>): List<Target> {
        val entities = targets.map { TargetEntity.from(it) }
        return targetJpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun deleteAllByCourseCode(courseCode: Long) {
        targetJpaRepository.deleteAllByCourseCode(courseCode)
    }
}

interface TargetJpaRepository : JpaRepository<TargetEntity, Long> {
    fun deleteAllByCourseCode(courseCode: Long)
}
