package com.yourssu.soongpt.domain.admin.business

import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.course.business.dto.*
import com.yourssu.soongpt.domain.course.business.query.SearchCoursesQuery
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminCourseServiceImpl(
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
    private val collegeReader: CollegeReader,
) : AdminCourseService {

    /**
     * 관리자 과목 검색
     * - 사용자 검색과 달리 채플(baseCode)도 포함한다.
     */
    override fun search(query: SearchCoursesQuery): SearchCoursesResponse {
        val page = courseReader.searchCourses(query.query, query.toPageable())
        val courses = page.content

        val codes = courses.map { it.code }.distinct()
        val targetsByCode = targetReader.findAllByCodes(codes)
        val isStrictByCode =
            codes.associateWith { code ->
                (targetsByCode[code] ?: emptyList()).any { it.isStrict }
            }

        val grouped = courses.groupBy { it.baseCode() }
        val coursesList =
            grouped.map { (_, groupCourses) ->
                SearchCourseGroupResponse.from(groupCourses, isStrictByCode)
            }

        return SearchCoursesResponse(
            courses = coursesList,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            size = page.size,
            page = page.number,
        )
    }

    override fun getTargetsByCode(code: Long): CourseTargetResponse {
        val course = courseReader.findByCode(code)
        val targets = targetReader.findAllByCode(code)
        val courseTimes = CourseTimes.from(course.scheduleRoom)

        val targetInfos =
            targets.map { target ->
                val scopeName =
                    when (target.scopeType) {
                        ScopeType.UNIVERSITY -> "전체"
                        ScopeType.COLLEGE -> target.collegeId?.let { collegeReader.get(it).name }
                        ScopeType.DEPARTMENT ->
                            target.departmentId?.let { departmentReader.get(it).name }
                    }

                TargetInfo(
                    id = target.id,
                    scopeType = target.scopeType,
                    scopeId = target.departmentId ?: target.collegeId,
                    scopeName = scopeName,
                    grade1 = target.grade1,
                    grade2 = target.grade2,
                    grade3 = target.grade3,
                    grade4 = target.grade4,
                    grade5 = target.grade5,
                    studentType = target.studentType,
                    isStrict = target.isStrict,
                    isDenied = target.isDenied,
                )
            }

        val courseTimeResponses = courseTimes.toList().map { CourseTimeResponse.from(it) }

        return CourseTargetResponse(
            code = course.code,
            name = course.name,
            professor = course.professor,
            category = course.category,
            subCategory = course.subCategory,
            multiMajorCategory = course.multiMajorCategory,
            department = course.department,
            division = course.division,
            point = course.point,
            time = course.time,
            personeel = course.personeel,
            scheduleRoom = course.scheduleRoom,
            targetText = course.target,
            field = course.field,
            courseTimes = courseTimeResponses,
            targets = targetInfos,
        )
    }

    @Transactional
    override fun updateCourse(code: Long, command: UpdateCourseCommand): CourseDetailResponse {
        val existingCourse = courseReader.findByCode(code)

        val updatedCourse =
            existingCourse.copy(
                category = command.category,
                subCategory = command.subCategory,
                multiMajorCategory = command.multiMajorCategory ?: existingCourse.multiMajorCategory,
                field = command.field ?: "",
                name = command.name,
                professor = command.professor,
                department = command.department,
                division = command.division,
                time = command.time,
                point = command.point,
                personeel = command.personeel,
                scheduleRoom = command.scheduleRoom,
                target = command.target,
            )

        val savedCourse = courseReader.save(updatedCourse)
        val courseTimes = CourseTimes.from(savedCourse.scheduleRoom)
        return CourseDetailResponse.from(savedCourse, courseTimes)
    }

    @Transactional
    override fun updateTargets(code: Long, command: UpdateTargetsCommand): CourseTargetResponse {
        // Ensure course exists
        courseReader.findByCode(code)

        // Delete existing targets for this course
        targetReader.deleteAllByCourseCode(code)

        // Create new targets from command
        val newTargets =
            command.targets.map {
                val scopeId =
                    if (it.scopeId != null) {
                        it.scopeId
                    } else if (it.scopeName != null) {
                        when (it.scopeType) {
                            ScopeType.COLLEGE -> collegeReader.getByName(it.scopeName).id
                            ScopeType.DEPARTMENT -> departmentReader.getByName(it.scopeName).id
                            else -> null
                        }
                    } else {
                        null
                    }

                Target(
                    courseCode = code,
                    id = it.id,
                    scopeType = it.scopeType,
                    collegeId = if (it.scopeType == ScopeType.COLLEGE) scopeId else null,
                    departmentId = if (it.scopeType == ScopeType.DEPARTMENT) scopeId else null,
                    grade1 = it.grade1,
                    grade2 = it.grade2,
                    grade3 = it.grade3,
                    grade4 = it.grade4,
                    grade5 = it.grade5,
                    studentType = it.studentType,
                    isStrict = it.isStrict,
                    isDenied = it.isDenied,
                )
            }

        // Save new targets
        targetReader.saveAll(newTargets)

        return getTargetsByCode(code)
    }

    @Transactional
    override fun createCourse(command: CreateCourseCommand): CourseDetailResponse {
        val newCourse =
            com.yourssu.soongpt.domain.course.implement.Course(
                id = null,
                category = command.category,
                subCategory = command.subCategory,
                multiMajorCategory = command.multiMajorCategory,
                field = command.field ?: "",
                code = command.code,
                name = command.name,
                professor = command.professor,
                department = command.department,
                division = command.division,
                time = command.time,
                point = command.point,
                personeel = command.personeel,
                scheduleRoom = command.scheduleRoom,
                target = command.target,
            )

        val savedCourse = courseReader.save(newCourse)
        val courseTimes = CourseTimes.from(savedCourse.scheduleRoom)
        return CourseDetailResponse.from(savedCourse, courseTimes)
    }

    @Transactional
    override fun deleteCourse(code: Long) {
        // Delete related targets first
        targetReader.deleteAllByCourseCode(code)
        // Delete the course
        courseReader.delete(code)
    }
}
