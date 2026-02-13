package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.course.business.dto.*
import com.yourssu.soongpt.domain.course.business.query.*
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import com.yourssu.soongpt.domain.course.implement.baseCode
import com.yourssu.soongpt.domain.course.implement.toBaseCode
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.ScopeType
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseServiceImpl(
        private val courseReader: CourseReader,
        private val departmentReader: DepartmentReader,
        private val targetReader: TargetReader,
        private val collegeReader: CollegeReader,
        private val courseFieldReader: CourseFieldReader,
) : CourseService {
    override fun findAll(query: FilterCoursesQuery): List<CourseResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses =
                if (query.field != null) {
                    courseReader.findAllInCategory(
                            query.category,
                            courseCodes,
                            query.field,
                            query.schoolId
                    )
                } else {
                    courseReader.findAllInCategory(query.category, courseCodes, query.schoolId)
                }

        return courses.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom)
            CourseResponse.from(course, courseTimes.toList())
        }
    }

    override fun findAllByTrack(query: FilterCoursesByTrackQuery): List<CourseResponse> {
        val department = departmentReader.getByName(query.departmentName)

        // 타전공인정은 원본 분류(course_secondary_major_classification) 기준으로만 조회
        // - target allow/deny/scope/grade 필터를 적용하지 않아 원본 인정 목록과 동일하게 노출
        if (query.trackType == SecondaryMajorTrackType.CROSS_MAJOR) {
            val completionTypes = query.completionType?.let { listOf(it) }
                ?: listOf(SecondaryMajorCompletionType.RECOGNIZED)

            val courses = completionTypes
                .flatMap { completionType ->
                    courseReader.findCoursesBySecondaryMajorClassification(
                        trackType = query.trackType,
                        completionType = completionType,
                        departmentId = department.id!!,
                    )
                }
                .distinctBy { it.code }

            return courses.map { course ->
                val courseTimes = CourseTimes.from(course.scheduleRoom)
                CourseResponse.from(course, courseTimes.toList())
            }
        }

        val college = collegeReader.get(department.collegeId)

        // 전체 학년(1-5) 조회
        val allGrades = (1..5)

        // completionType이 지정된 경우 해당 이수구분만 조회
        if (query.completionType != null) {
            val coursesWithTarget =
                    allGrades.flatMap { grade ->
                        courseReader.findCoursesWithTargetBySecondaryMajor(
                                trackType = query.trackType,
                                completionType = query.completionType,
                                departmentId = department.id!!,
                                collegeId = college.id!!,
                                maxGrade = grade,
                        )
                    }

            // 중복 제거 (같은 과목 코드)
            val uniqueCourses = coursesWithTarget.distinctBy { it.course.code }

            return uniqueCourses.map { courseWithTarget ->
                val courseTimes = CourseTimes.from(courseWithTarget.course.scheduleRoom)
                CourseResponse.from(courseWithTarget.course, courseTimes.toList())
            }
        }

        // completionType이 없으면 해당 trackType의 모든 이수구분 조회
        val allCompletionTypes =
                when (query.trackType) {
                    SecondaryMajorTrackType.DOUBLE_MAJOR,
                    SecondaryMajorTrackType.MINOR ->
                            listOf(
                                    SecondaryMajorCompletionType.REQUIRED,
                                    SecondaryMajorCompletionType.ELECTIVE
                            )
                    SecondaryMajorTrackType.CROSS_MAJOR ->
                            listOf(SecondaryMajorCompletionType.RECOGNIZED)
                }

        val allCourses =
                allGrades.flatMap { grade ->
                    allCompletionTypes.flatMap { completionType ->
                        courseReader.findCoursesWithTargetBySecondaryMajor(
                                trackType = query.trackType,
                                completionType = completionType,
                                departmentId = department.id!!,
                                collegeId = college.id!!,
                                maxGrade = grade,
                        )
                    }
                }

        // 중복 제거 (같은 과목 코드)
        val uniqueCourses = allCourses.distinctBy { it.course.code }

        return uniqueCourses.map { courseWithTarget ->
            val courseTimes = CourseTimes.from(courseWithTarget.course.scheduleRoom)
            CourseResponse.from(courseWithTarget.course, courseTimes.toList())
        }
    }

    override fun search(query: SearchCoursesQuery): SearchCoursesResponse {
        val page = courseReader.searchCourses(query.query, query.toPageable())
        val courses = page.content.filter { it.baseCode() !in CHAPEL_BASE_CODES }

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

    override fun findAllByCode(codes: List<Long>): List<CourseDetailResponse> {
        val courses = courseReader.findAllByCode(codes)

        val responses = mutableListOf<CourseDetailResponse>()
        for (course in courses) {
            val courseTimes = CourseTimes.from(course.scheduleRoom)
            responses.add(CourseDetailResponse.from(course, courseTimes))
        }

        return responses
    }

    override fun getFields(schoolId: Int?): Any {
        if (schoolId != null) {
            return courseReader.getFieldsBySchoolId(schoolId)
        }
        return courseReader.getAllFieldsGrouped()
    }

    override fun getAllFieldsGrouped(): Map<Int, List<String>> {
        return courseReader.getAllFieldsGrouped()
    }

    override fun getFieldByCourseCode(courseCode: Long, schoolId: Int): String? {
        // 분반 코드(10자리)가 들어와도 동작하도록 8자리 기본코드로 한번 더 조회
        val courseField =
                courseFieldReader.findByCourseCode(courseCode)
                        ?: courseFieldReader.findByCourseCode(courseCode.toBaseCode())
                                ?: return null

        return FieldFinder.findFieldBySchoolId(courseField.field, schoolId)
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
                                ScopeType.COLLEGE ->
                                        target.collegeId?.let { collegeReader.get(it).name }
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
                            isDenied = target.isDenied
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
                targets = targetInfos
        )
    }

    @Deprecated("Use findAll with FilterCoursesQuery instead")
    fun findAll(query: GeneralRequiredCourseQuery): List<GeneralRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        if (query.field == null) {
            val courses =
                    courseReader.findAllInCategory(
                            Category.GENERAL_REQUIRED,
                            courseCodes,
                            query.schoolId
                    )
            return courses.map { GeneralRequiredResponse.from(it) }
        }
        val courses =
                courseReader.findAllInCategory(
                        Category.GENERAL_REQUIRED,
                        courseCodes,
                        query.field,
                        query.schoolId
                )
        return courses.map { GeneralRequiredResponse.from(it) }
    }

    @Deprecated("Use findAll with FilterCoursesQuery instead")
    fun findAll(query: MajorRequiredCourseQuery): List<MajorRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses =
                courseReader.findAllInCategory(Category.MAJOR_REQUIRED, courseCodes, query.schoolId)
        return courses.map { MajorRequiredResponse.from(it) }
    }

    @Deprecated("Use findAll with FilterCoursesQuery instead")
    fun findAll(query: MajorElectiveCourseQuery): List<MajorElectiveResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses =
                courseReader.findAllInCategory(Category.MAJOR_ELECTIVE, courseCodes, query.schoolId)
        return courses.map { MajorElectiveResponse.from(it) }
    }

    @Transactional
    override fun updateCourse(code: Long, command: UpdateCourseCommand): CourseDetailResponse {
        val existingCourse = courseReader.findByCode(code)

        val updatedCourse =
                existingCourse.copy(
                        category = command.category,
                        subCategory = command.subCategory,
                        multiMajorCategory = command.multiMajorCategory
                                        ?: existingCourse.multiMajorCategory,
                        field = command.field ?: "",
                        name = command.name,
                        professor = command.professor,
                        department = command.department,
                        division = command.division,
                        time = command.time,
                        point = command.point,
                        personeel = command.personeel,
                        scheduleRoom = command.scheduleRoom,
                        target = command.target
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
                                    ScopeType.DEPARTMENT ->
                                            departmentReader.getByName(it.scopeName).id
                                    else -> null
                                }
                            } else {
                                null
                            }

                    com.yourssu.soongpt.domain.target.implement.Target(
                            courseCode = code,
                            id = it.id,
                            scopeType = it.scopeType,
                            collegeId = if (it.scopeType == ScopeType.COLLEGE) scopeId else null,
                            departmentId =
                                    if (it.scopeType == ScopeType.DEPARTMENT) scopeId else null,
                            grade1 = it.grade1,
                            grade2 = it.grade2,
                            grade3 = it.grade3,
                            grade4 = it.grade4,
                            grade5 = it.grade5,
                            studentType = it.studentType,
                            isStrict = it.isStrict,
                            isDenied = it.isDenied
                    )
                }

        // Save new targets
        targetReader.saveAll(newTargets)

        // Return updated target response
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
                        target = command.target
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

    override fun findAllTeachingCourses(query: FilterTeachingCoursesQuery): List<CourseResponse> {
        val department = departmentReader.getByName(query.departmentName)

        // 전체 학년(1-5) 조회 - GENERAL + TEACHING_CERT 타입 모두 조회
        val allGrades = (1..5)
        val allCourseCodes =
                allGrades
                        .flatMap { grade ->
                            targetReader.findAllByDepartmentGrade(department, grade) +
                                    targetReader.findAllByDepartmentGradeForTeaching(
                                            department,
                                            grade
                                    )
                        }
                        .distinct()

        // TEACHING 카테고리의 모든 과목 조회
        val courses =
                courseReader.findAllInCategory(Category.TEACHING, allCourseCodes, query.schoolId)

        // majorArea로 필터링 (field 기반)
        val filteredCourses =
                if (query.majorArea != null) {
                    val targetFields = query.majorArea.fieldValues
                    courses.filter { course ->
                        targetFields.any { fieldValue ->
                            course.field?.contains(fieldValue) == true
                        }
                    }
                } else {
                    courses
                }

        return filteredCourses.map { course ->
            val courseTimes = CourseTimes.from(course.scheduleRoom)
            CourseResponse.from(course, courseTimes.toList())
        }
    }

    companion object {
        private val CHAPEL_BASE_CODES = setOf(21500785L, 21501015L)
    }
}
