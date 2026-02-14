package com.yourssu.soongpt.domain.course.storage

import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.implement.*
import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.course.storage.QCourseEntity.courseEntity
import com.yourssu.soongpt.domain.course.storage.QCourseSecondaryMajorClassificationEntity.courseSecondaryMajorClassificationEntity
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.StudentType
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.orm.jpa.JpaSystemException

@Component
class CourseRepositoryImpl(
    private val courseJpaRepository: CourseJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
    ): CourseRepository {
    private val logger = KotlinLogging.logger {}
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
        val sanitizedQuery = query.trim()
        if (sanitizedQuery.isBlank()) {
            return findAll(pageable)
        }

        // 5자리 이상 10자리 이하 숫자이면 코드 프리픽스로 범위 검색
        // 10자리 코드 기준 prefix → range 계산
        // "21500" → 2150000000..2150099999
        // "21500785" → 2150078500..2150078599 (기존과 동일)
        val codeDigits = sanitizedQuery.takeIf { it.length in 5..10 && it.all { c -> c.isDigit() } }

        if (codeDigits != null) {
            val num = codeDigits.toLong()
            val padLen = 10 - codeDigits.length
            var multiplier = 1L
            repeat(padLen) { multiplier *= 10 }
            val codeRangeStart = num * multiplier
            val codeRangeEnd = (num + 1) * multiplier - 1

            try {
                return loadSearchResultPage(
                    pageable = pageable,
                    fetch = {
                        courseJpaRepository
                            .searchCoursesWithFulltextByCodeRange(
                                sanitizedQuery,
                                codeRangeStart,
                                codeRangeEnd,
                                fetchLimit,
                                pageable.offset,
                            )
                            .map { it.toDomain() }
                    },
                    count = {
                        courseJpaRepository.countCoursesWithFulltextByCodeRange(
                            sanitizedQuery,
                            codeRangeStart,
                            codeRangeEnd,
                        )
                    },
                )
            } catch (exception: JpaSystemException) {
                if (!isFulltextIndexMissingException(exception)) throw exception
                logger.warn { "Course search with FULLTEXT index failed for code-range query, fallback to LIKE query: ${exception.message}" }
                return searchCoursesByLikeFallbackWithCodeRange(
                    query = sanitizedQuery,
                    codeRangeStart = codeRangeStart,
                    codeRangeEnd = codeRangeEnd,
                    fetchLimit = fetchLimit,
                    pageable = pageable,
                )
            }
        }
        try {
            return loadSearchResultPage(
                pageable = pageable,
                fetch = {
                    courseJpaRepository
                        .searchCoursesWithFulltext(sanitizedQuery, fetchLimit, pageable.offset)
                        .map { it.toDomain() }
                },
                count = {
                    courseJpaRepository.countCoursesWithFulltext(sanitizedQuery)
                },
            )
        } catch (exception: JpaSystemException) {
            if (!isFulltextIndexMissingException(exception)) throw exception
            logger.warn { "Course search with FULLTEXT index failed, fallback to LIKE query: ${exception.message}" }
            return searchCoursesByLikeFallback(
                query = sanitizedQuery,
                fetchLimit = fetchLimit,
                pageable = pageable,
            )
        }
    }

    private fun isFulltextIndexMissingException(exception: JpaSystemException): Boolean {
        val messageContainsFulltextError = "can't find fulltext index matching the column list"
        return generateSequence<Throwable>(exception) { it.cause }
            .mapNotNull { it.message?.lowercase() }
            .any { it.contains(messageContainsFulltextError) }
    }

    private fun loadSearchResultPage(
        pageable: Pageable,
        fetch: () -> List<Course>,
        count: () -> Long,
    ): PageImpl<Course> {
        val results = fetch()
        val hasNext = results.size > pageable.pageSize
        val content = if (hasNext) results.dropLast(1) else results
        val total = if (hasNext || pageable.offset > 0) count() else content.size.toLong()
        return PageImpl(content, pageable, total)
    }

    private fun searchCoursesByLikeFallbackWithCodeRange(
        query: String,
        codeRangeStart: Long,
        codeRangeEnd: Long,
        fetchLimit: Int,
        pageable: Pageable,
    ): PageImpl<Course> {
        return loadSearchResultPage(
            pageable = pageable,
            fetch = {
                searchCoursesFallbackByCodeRange(
                    query = query,
                    codeRangeStart = codeRangeStart,
                    codeRangeEnd = codeRangeEnd,
                    fetchLimit = fetchLimit,
                    offset = pageable.offset,
                )
            },
            count = {
                countCoursesFallbackByCodeRange(
                    query = query,
                    codeRangeStart = codeRangeStart,
                    codeRangeEnd = codeRangeEnd,
                )
            },
        )
    }

    private fun searchCoursesByLikeFallback(
        query: String,
        fetchLimit: Int,
        pageable: Pageable,
    ): PageImpl<Course> {
        return loadSearchResultPage(
            pageable = pageable,
            fetch = {
                searchCoursesFallback(
                    query = query,
                    fetchLimit = fetchLimit,
                    offset = pageable.offset,
                )
            },
            count = {
                countCoursesFallback(query)
            },
        )
    }

    private fun searchCoursesFallbackByCodeRange(
        query: String,
        codeRangeStart: Long,
        codeRangeEnd: Long,
        fetchLimit: Int,
        offset: Long,
    ): List<Course> {
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(
                courseEntity.code.between(codeRangeStart, codeRangeEnd)
                    .or(courseEntity.name.containsIgnoreCase(query))
                    .or(courseEntity.professor.containsIgnoreCase(query)),
            )
            .orderBy(
                com.querydsl.core.types.dsl.CaseBuilder()
                    .`when`(courseEntity.code.between(codeRangeStart, codeRangeEnd)).then(0).otherwise(1).asc(),
                com.querydsl.core.types.dsl.CaseBuilder()
                    .`when`(courseEntity.name.lower().startsWith(query.lowercase())).then(0).otherwise(1).asc(),
                com.querydsl.core.types.dsl.CaseBuilder()
                    .`when`(courseEntity.professor.lower().startsWith(query.lowercase())).then(0).otherwise(1).asc(),
                courseEntity.name.length().asc(),
                courseEntity.name.asc(),
            )
            .limit(fetchLimit.toLong())
            .offset(offset)
            .fetch()
            .map { it.toDomain() }
    }

    private fun countCoursesFallbackByCodeRange(
        query: String,
        codeRangeStart: Long,
        codeRangeEnd: Long,
    ): Long {
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(
                courseEntity.code.between(codeRangeStart, codeRangeEnd)
                    .or(courseEntity.name.containsIgnoreCase(query))
                    .or(courseEntity.professor.containsIgnoreCase(query)),
            )
            .fetch()
            .size
            .toLong()
    }

    private fun searchCoursesFallback(
        query: String,
        fetchLimit: Int,
        offset: Long,
    ): List<Course> {
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(
                courseEntity.name.containsIgnoreCase(query)
                    .or(courseEntity.professor.containsIgnoreCase(query)),
            )
            .orderBy(
                com.querydsl.core.types.dsl.CaseBuilder()
                    .`when`(courseEntity.name.lower().startsWith(query.lowercase())).then(0).otherwise(1).asc(),
                com.querydsl.core.types.dsl.CaseBuilder()
                    .`when`(courseEntity.professor.lower().startsWith(query.lowercase())).then(0).otherwise(1).asc(),
                courseEntity.name.length().asc(),
                courseEntity.name.asc(),
            )
            .limit(fetchLimit.toLong())
            .offset(offset)
            .fetch()
            .map { it.toDomain() }
    }

    private fun countCoursesFallback(
        query: String,
    ): Long {
        return jpaQueryFactory
            .selectFrom(courseEntity)
            .where(
                courseEntity.name.containsIgnoreCase(query)
                    .or(courseEntity.professor.containsIgnoreCase(query)),
            )
            .fetch()
            .size
            .toLong()
    }

    override fun findAllByClass(code: Long): List<Course> {
        val codeWithoutDivision = if (code.toString().length > 8) {
            code / DIVISION_DIVISOR
        } else {
            code
        }
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

        // Allow 과목 조회 (Target 정보 포함, Deny 제외)
        val allowResults = jpaQueryFactory
            .select(
                Projections.tuple(
                    courseEntity,
                    targetEntity.grade1,
                    targetEntity.grade2,
                    targetEntity.grade3,
                    targetEntity.grade4,
                    targetEntity.grade5,
                    targetEntity.isStrict,
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
        // - CHAPEL은 현재 학년만 적용 (grade1 deny가 2~4학년까지 확장되는 문제 방지. or로 걸어버리는 문제가 있음.)
        // - 그 외는 기존처럼 누적 학년 범위 적용
        val denyGradeCondition = if (category == Category.CHAPEL) {
            buildGradeExactCondition(maxGrade)
        } else {
            gradeCondition
        }

        val denyCodes = jpaQueryFactory
            .select(targetEntity.courseCode)
            .from(targetEntity)
            .where(
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isTrue,
                denyGradeCondition,
                scopeCondition,
            )
            .fetch()
            .toSet()

        // CourseWithTarget 변환 (Deny 과목 제외, 같은 과목코드의 targetGrades 병합)
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
                    isStrict = tuple.get(targetEntity.isStrict) ?: false,
                )
            }
            .groupBy { it.course.code }
            .map { (courseCode, targets) ->
                // 같은 course.code인데 course 정보가 다른 경우 경고 로그
                val uniqueCourses = targets.map { it.course }.distinctBy { "${it.name}|${it.department}" }
                if (uniqueCourses.size > 1) {
                    logger.warn {
                        "같은 과목코드($courseCode)인데 course 정보가 다릅니다. " +
                            "과목들: ${uniqueCourses.joinToString(", ") { "${it.name}(${it.department})" }}"
                    }
                }
                targets.first().copy(
                    targetGrades = targets.flatMap { it.targetGrades }.distinct().sorted(),
                    isStrict = targets.any { it.isStrict },
                )
            }
    }

    override fun findCoursesWithTargetBySecondaryMajor(
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        departmentId: Long,
        collegeId: Long,
        maxGrade: Int,
    ): List<CourseWithTarget> {
        // 타전공인정과목의 경우 다른 학과의 과목이므로 scopeCondition을 완화
        // UNIVERSITY 또는 COLLEGE 범위만 확인하고, DEPARTMENT 범위는 확인하지 않음
        val scopeCondition = if (trackType == SecondaryMajorTrackType.CROSS_MAJOR) {
            buildScopeConditionForCrossMajor(collegeId)
        } else {
            buildScopeCondition(departmentId, collegeId)
        }
        val gradeCondition = buildGradeRangeCondition(maxGrade)

        val allowResults = jpaQueryFactory
            .select(
                Projections.tuple(
                    courseEntity,
                    targetEntity.grade1,
                    targetEntity.grade2,
                    targetEntity.grade3,
                    targetEntity.grade4,
                    targetEntity.grade5,
                    targetEntity.isStrict,
                )
            )
            .from(courseEntity)
            .innerJoin(courseSecondaryMajorClassificationEntity)
            .on(courseEntity.code.eq(courseSecondaryMajorClassificationEntity.courseCode))
            .innerJoin(targetEntity).on(courseEntity.code.eq(targetEntity.courseCode))
            .where(
                courseSecondaryMajorClassificationEntity.trackType.eq(trackType),
                courseSecondaryMajorClassificationEntity.completionType.eq(completionType),
                courseSecondaryMajorClassificationEntity.departmentId.eq(departmentId),
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isFalse,
                gradeCondition,
                scopeCondition,
            )
            .fetch()

        if (allowResults.isEmpty()) {
            return emptyList()
        }

        // Deny 조건: scopeCondition 재사용 (중복 계산 방지)
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

        // CourseWithTarget 변환 (Deny 과목 제외, 같은 과목코드의 targetGrades 병합)
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
                    isStrict = tuple.get(targetEntity.isStrict) ?: false,
                )
            }
            .groupBy { it.course.code }
            .map { (courseCode, targets) ->
                // 같은 course.code인데 course 정보가 다른 경우 경고 로그
                val uniqueCourses = targets.map { it.course }.distinctBy { "${it.name}|${it.department}" }
                if (uniqueCourses.size > 1) {
                    logger.warn {
                        "같은 과목코드($courseCode)인데 course 정보가 다릅니다. " +
                            "과목들: ${uniqueCourses.joinToString(", ") { "${it.name}(${it.department})" }}"
                    }
                }
                targets.first().copy(
                    targetGrades = targets.flatMap { it.targetGrades }.distinct().sorted(),
                    isStrict = targets.any { it.isStrict },
                )
            }
    }

    override fun findCoursesBySecondaryMajorClassification(
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        departmentId: Long,
    ): List<Course> {
        return jpaQueryFactory
            .selectDistinct(courseEntity)
            .from(courseEntity)
            .innerJoin(courseSecondaryMajorClassificationEntity)
            .on(courseEntity.code.eq(courseSecondaryMajorClassificationEntity.courseCode))
            .where(
                courseSecondaryMajorClassificationEntity.trackType.eq(trackType),
                courseSecondaryMajorClassificationEntity.completionType.eq(completionType),
                courseSecondaryMajorClassificationEntity.departmentId.eq(departmentId),
            )
            .fetch()
            .map { it.toDomain() }
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

    /**
     * 타전공인정과목용 scope 조건
     * - 타전공인정과목은 다른 학과의 과목이므로 DEPARTMENT 범위는 확인하지 않음
     * - UNIVERSITY 또는 COLLEGE 범위만 확인
     */
    private fun buildScopeConditionForCrossMajor(collegeId: Long): BooleanExpression {
        return targetEntity.scopeType.eq(ScopeType.UNIVERSITY)
            .or(
                targetEntity.scopeType.eq(ScopeType.COLLEGE)
                    .and(targetEntity.collegeId.eq(collegeId))
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

    private fun buildGradeExactCondition(grade: Int): BooleanExpression {
        return when (grade) {
            1 -> targetEntity.grade1.isTrue
            2 -> targetEntity.grade2.isTrue
            3 -> targetEntity.grade3.isTrue
            4 -> targetEntity.grade4.isTrue
            5 -> targetEntity.grade5.isTrue
            else -> targetEntity.grade1.isTrue
        }
    }

    override fun findDepartmentIdsByTrackType(trackType: SecondaryMajorTrackType): List<Long> {
        return jpaQueryFactory
            .select(courseSecondaryMajorClassificationEntity.departmentId)
            .from(courseSecondaryMajorClassificationEntity)
            .where(courseSecondaryMajorClassificationEntity.trackType.eq(trackType))
            .distinct()
            .fetch()
    }

    override fun findCoursesWithTargetByBaseCodes(baseCodes: List<Long>): List<CourseWithTarget> {
        if (baseCodes.isEmpty()) return emptyList()

        // baseCode = code / 100 이므로, code / 100 IN baseCodes 조건 사용
        val baseCodeCondition = courseEntity.code.divide(DIVISION_DIVISOR).longValue().`in`(baseCodes)

        val results = jpaQueryFactory
            .select(
                Projections.tuple(
                    courseEntity,
                    targetEntity.grade1,
                    targetEntity.grade2,
                    targetEntity.grade3,
                    targetEntity.grade4,
                    targetEntity.grade5,
                    targetEntity.isStrict,
                )
            )
            .from(courseEntity)
            .innerJoin(targetEntity).on(courseEntity.code.eq(targetEntity.courseCode))
            .where(
                baseCodeCondition,
                targetEntity.studentType.eq(StudentType.GENERAL),
                targetEntity.isDenied.isFalse,
            )
            .fetch()

        if (results.isEmpty()) return emptyList()

        return results.map { tuple ->
            CourseWithTarget(
                course = tuple.get(courseEntity)!!.toDomain(),
                targetGrades = CourseWithTarget.extractTargetGrades(
                    grade1 = tuple.get(targetEntity.grade1) ?: false,
                    grade2 = tuple.get(targetEntity.grade2) ?: false,
                    grade3 = tuple.get(targetEntity.grade3) ?: false,
                    grade4 = tuple.get(targetEntity.grade4) ?: false,
                    grade5 = tuple.get(targetEntity.grade5) ?: false,
                ),
                isStrict = tuple.get(targetEntity.isStrict) ?: false,
            )
        }
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
            WHERE (code BETWEEN :codeRangeStart AND :codeRangeEnd)
                OR MATCH(name, professor, department) AGAINST(:query IN BOOLEAN MODE)
            ORDER BY
                CASE WHEN code BETWEEN :codeRangeStart AND :codeRangeEnd THEN 0 ELSE 1 END,
                CASE WHEN LOWER(name) LIKE CONCAT(LOWER(:query), '%') THEN 0 ELSE 1 END,
                CASE WHEN professor IS NOT NULL AND LOWER(professor) LIKE CONCAT(LOWER(:query), '%') THEN 0 ELSE 1 END,
                CASE WHEN LOWER(department) LIKE CONCAT(LOWER(:query), '%') THEN 0 ELSE 1 END,
                CHAR_LENGTH(name),
                LOWER(name)
            LIMIT :limit OFFSET :offset
        """,
        nativeQuery = true
    )
    fun searchCoursesWithFulltextByCodeRange(
        query: String,
        codeRangeStart: Long,
        codeRangeEnd: Long,
        limit: Int,
        offset: Long,
    ): List<CourseEntity>

    @Query(
        value = """
            SELECT COUNT(*) FROM course
            WHERE (code BETWEEN :codeRangeStart AND :codeRangeEnd)
                OR MATCH(name, professor, department) AGAINST(:query IN BOOLEAN MODE)
        """,
        nativeQuery = true
    )
    fun countCoursesWithFulltextByCodeRange(query: String, codeRangeStart: Long, codeRangeEnd: Long): Long

    @Query(
        value = """
            SELECT * FROM course
            WHERE MATCH(name, professor, department) AGAINST(:query IN BOOLEAN MODE)
            ORDER BY
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
            WHERE MATCH(name, professor, department) AGAINST(:query IN BOOLEAN MODE)
        """,
        nativeQuery = true
    )
    fun countCoursesWithFulltext(query: String): Long
}
