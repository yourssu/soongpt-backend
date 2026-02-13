package com.yourssu.soongpt.domain.admin.business

import com.yourssu.soongpt.domain.course.business.dto.*
import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery

/**
 * 관리자용 과목 서비스.
 *
 * - Course 도메인 서비스(CourseService)와 분리하여, Admin API는 이 서비스만 의존하도록 한다.
 * - 관리자 페이지에서는 채플 과목도 조회되어야 하므로(admin search), 사용자 검색과 정책을 분리한다.
 */
interface AdminCourseService {
    fun search(query: SearchCoursesQuery): SearchCoursesResponse
    fun getTargetsByCode(code: Long): CourseTargetResponse
    fun updateCourse(code: Long, command: UpdateCourseCommand): CourseDetailResponse
    fun updateTargets(code: Long, command: UpdateTargetsCommand): CourseTargetResponse
    fun createCourse(command: CreateCourseCommand): CourseDetailResponse
    fun deleteCourse(code: Long)
}
