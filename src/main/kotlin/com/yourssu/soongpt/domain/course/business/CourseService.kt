package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.*
import com.yourssu.soongpt.domain.course.business.query.FilterCoursesByTrackQuery
import com.yourssu.soongpt.domain.course.business.query.FilterCoursesQuery
import com.yourssu.soongpt.domain.course.business.query.FilterTeachingCoursesQuery
import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery

interface CourseService {
    fun findAll(query: FilterCoursesQuery): List<CourseResponse>
    fun findAllByTrack(query: FilterCoursesByTrackQuery): List<CourseResponse>
    fun findAllTeachingCourses(query: FilterTeachingCoursesQuery): List<CourseResponse>
    fun search(query: SearchCoursesQuery): SearchCoursesResponse
    fun findAllByCode(codes: List<Long>): List<CourseDetailResponse>
    fun getFields(schoolId: Int?): Any
    fun getAllFieldsGrouped(): Map<Int, List<String>>
    fun getFieldByCourseCode(courseCode: Long, schoolId: Int): String?
    fun getTargetsByCode(code: Long): CourseTargetResponse
    fun updateCourse(code: Long, command: UpdateCourseCommand): CourseDetailResponse
    fun updateTargets(code: Long, command: UpdateTargetsCommand): CourseTargetResponse
    fun createCourse(command: CreateCourseCommand): CourseDetailResponse
    fun deleteCourse(code: Long)
}
