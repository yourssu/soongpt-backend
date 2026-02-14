package com.yourssu.soongpt.domain.course.implement

import com.yourssu.soongpt.domain.course.implement.dto.GroupedCoursesByCategoryDto
import com.yourssu.soongpt.domain.department.implement.Department
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CourseRepository {
    fun get(code: Long): Course
    fun findAll(pageable: Pageable): Page<Course>
    fun findAllById(courseIds: List<Long>): List<Course>
    fun findAllByCode(codes: List<Long>): List<Course>
    fun findAllInCategory(category: Category, courseCodes: List<Long>): List<Course>
    fun groupByCategory(codes: List<Long>): GroupedCoursesByCategoryDto
    fun searchCourses(query: String, pageable: Pageable): Page<Course>
    fun findAllByClass(code: Long): List<Course>

    /**
     * 학과/단과대/전체 범위에서 특정 카테고리의 과목을 Target 정보와 함께 조회
     * - Target join으로 대상 학년 정보 포함
     * - Allow - Deny 로직 적용
     */
    fun findCoursesWithTargetByCategory(
        category: Category,
        departmentId: Long,
        collegeId: Long,
        userGrade: Int,
        maxGrade: Int,
    ): List<CourseWithTarget>

    /**
     * 복수전공/부전공/타전공인정 이수구분 기반 과목 조회
     * - course_secondary_major_classification + target + course join
     * - Allow - Deny 적용
     */
    fun findCoursesWithTargetBySecondaryMajor(
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        departmentId: Long,
        collegeId: Long,
        userGrade: Int,
        maxGrade: Int,
    ): List<CourseWithTarget>

    /**
     * 이수구분 분류 테이블 기준 과목 조회 (Target 미적용)
     * - course_secondary_major_classification + course join
     * - CROSS_MAJOR 조회용 (원본 인정 목록 노출)
     */
    fun findCoursesBySecondaryMajorClassification(
        trackType: SecondaryMajorTrackType,
        completionType: SecondaryMajorCompletionType,
        departmentId: Long,
    ): List<Course>

    /**
     * baseCode 목록으로 과목을 Target 정보와 함께 조회 (재수강용)
     * - code / 100 IN baseCodes 조건으로 분반 포함 조회
     * - Allow - Deny 로직 적용
     */
    fun findCoursesWithTargetByBaseCodes(baseCodes: List<Long>): List<CourseWithTarget>

    /**
     * 특정 trackType에 해당하는 학과 ID 목록 조회
     */
    fun findDepartmentIdsByTrackType(trackType: SecondaryMajorTrackType): List<Long>

    fun save(course: Course): Course
    fun delete(code: Long)
}
