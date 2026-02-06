package com.yourssu.soongpt.domain.course.storage

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.*
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
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
    private val totalCount: Long = courseJpaRepository.count()

    override fun get(code: Long): Course {
        return courseJpaRepository.getByCode(code).toDomain()
    }

//    @Cacheable(value = ["courseCache"], key = "#pageable.pageNumber + '_' + #pageable.pageSize")
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
        val fetchLimit = pageable.pageSize + 1

        val results = courseJpaRepository
            .searchCoursesWithFulltext(query, fetchLimit, pageable.offset)
            .map { it.toDomain() }

        val hasNext = results.size > pageable.pageSize
        val content = if (hasNext) results.dropLast(1) else results

        val total = if (hasNext || pageable.offset > 0) {
            courseJpaRepository.countCoursesWithFulltext(query)
        } else {
            content.size.toLong()
        }

        return PageImpl(content, pageable, total)
    }

    override fun findAllByClass(code: Long): List<Course> {
        val codeWithoutDivision = code.div(DIVISION_DIVISOR)
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(courseEntity.code.divide(DIVISION_DIVISOR).longValue().eq(codeWithoutDivision))
            .fetch()
            .map { it.toDomain() }
    }

    override fun findCoursesWithTargetByCategory(
        category: Category,
        departmentId: Long,
        collegeId: Long,
        maxGrade: Int,
    ): List<CourseWithTarget> {
        val scopeCondition = buildScopeCondition(departmentId, collegeId)
        val gradeCondition = buildGradeRangeCondition(maxGrade)

        // Allow 과목 조회 (Target 정보 포함)
        val allowResults = jpaQueryFactory
            .select(
                Projections.tuple(
                    courseEntity,
                    targetEntity.grade1,
                    targetEntity.grade2,
                    targetEntity.grade3,
                    targetEntity.grade4,
                    targetEntity.grade5,
                )
            )
            .from(courseEntity)
            .innerJoin(targetEntity).on(courseEntity.code.eq(targetEntity.courseCode))
            .where(
                courseEntity.category.eq(category),
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isFalse,
                gradeCondition,
                scopeCondition,
            )
            .fetch()

        if (allowResults.isEmpty()) {
            return emptyList()
        }

        // Deny 과목 코드 조회
        val denyCodes = jpaQueryFactory
            .select(targetEntity.courseCode)
            .from(targetEntity)
            .where(
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isTrue,
                gradeCondition,
                scopeCondition,
            )
            .fetch()
            .toSet()

        // Allow - Deny 적용 및 CourseWithTarget 변환
        return allowResults
            .filter { it.get(courseEntity)!!.code !in denyCodes }
            .map { tuple ->
                CourseWithTarget(
                    course = tuple.get(courseEntity)!!.toDomain(),
                    targetGrades = CourseWithTarget.extractTargetGrades(
                        grade1 = tuple.get(targetEntity.grade1) ?: false,
                        grade2 = tuple.get(targetEntity.grade2) ?: false,
                        grade3 = tuple.get(targetEntity.grade3) ?: false,
                        grade4 = tuple.get(targetEntity.grade4) ?: false,
                        grade5 = tuple.get(targetEntity.grade5) ?: false,
                    ),
                )
            }
    }

    private fun buildScopeCondition(departmentId: Long, collegeId: Long): BooleanExpression {
        return targetEntity.scopeType.eq(ScopeType.UNIVERSITY)
            .or(
                targetEntity.scopeType.eq(ScopeType.COLLEGE)
                    .and(targetEntity.collegeId.eq(collegeId))
            )
            .or(
                targetEntity.scopeType.eq(ScopeType.DEPARTMENT)
                    .and(targetEntity.departmentId.eq(departmentId))
            )
    }

    private fun buildGradeRangeCondition(maxGrade: Int): BooleanExpression {
        var condition = targetEntity.grade1.isTrue
        if (maxGrade >= 2) condition = condition.or(targetEntity.grade2.isTrue)
        if (maxGrade >= 3) condition = condition.or(targetEntity.grade3.isTrue)
        if (maxGrade >= 4) condition = condition.or(targetEntity.grade4.isTrue)
        if (maxGrade >= 5) condition = condition.or(targetEntity.grade5.isTrue)
        return condition
    }


    override fun save(course: Course): Course {
        return courseJpaRepository.save(CourseEntity.from(course)).toDomain()
    }

    override fun delete(code: Long) {
        val entity = courseJpaRepository.getByCode(code)
        courseJpaRepository.delete(entity)
    }

}

interface CourseJpaRepository: JpaRepository<CourseEntity, Long> {
    @Query("select c from CourseEntity c where c.code = :code")
    fun getByCode(code: Long): CourseEntity

    @Query("select c from CourseEntity c where c.code in :codes")
    fun getAllByCode(codes: List<Long>): List<CourseEntity>

    @Query(
        value = """
            SELECT * FROM course
            WHERE MATCH(name, professor, department, target, schedule_room) AGAINST(:query IN BOOLEAN MODE)
                OR CAST(code AS CHAR) LIKE CONCAT(:query, '%')
            ORDER BY
                CASE WHEN CAST(code AS CHAR) = :query THEN 0 ELSE 1 END,
                CASE WHEN CAST(code AS CHAR) LIKE CONCAT(:query, '%') THEN 0 ELSE 1 END,
                CASE WHEN LOWER(name) LIKE CONCAT(LOWER(:query), '%') THEN 0 ELSE 1 END,
                CASE WHEN professor IS NOT NULL AND LOWER(professor) LIKE CONCAT(LOWER(:query), '%') THEN 0 ELSE 1 END,
                CASE WHEN LOWER(department) LIKE CONCAT(LOWER(:query), '%') THEN 0 ELSE 1 END,
                CHAR_LENGTH(name),
                LOWER(name)
            LIMIT :limit OFFSET :offset
        """,
        nativeQuery = true
    )
    fun searchCoursesWithFulltext(query: String, limit: Int, offset: Long): List<CourseEntity>

    @Query(
        value = """
            SELECT COUNT(*) FROM course
            WHERE MATCH(name, professor, department, target, schedule_room) AGAINST(:query IN BOOLEAN MODE)
                OR CAST(code AS CHAR) LIKE CONCAT(:query, '%')
        """,
        nativeQuery = true
    )
    fun countCoursesWithFulltext(query: String): Long
}
