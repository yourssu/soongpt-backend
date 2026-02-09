package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.college.implement.CollegeReader
import com.yourssu.soongpt.domain.course.business.dto.*
import com.yourssu.soongpt.domain.course.business.query.*
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.utils.FieldFinder
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.courseTime.business.dto.CourseTimeResponse
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
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

    override fun search(query: SearchCoursesQuery): SearchCoursesResponse {
        val page = courseReader.searchCourses(query.query, query.toPageable())
        return SearchCoursesResponse.from(page)
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
        val courseField = courseFieldReader.findByCourseCode(courseCode) ?: return null
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

        val courseTimeResponses = courseTimes.toList()
            .map { CourseTimeResponse.from(it) }

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
        val newCourse = com.yourssu.soongpt.domain.course.implement.Course(
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
}
