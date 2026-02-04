package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.business.dto.*
import com.yourssu.soongpt.domain.course.business.query.*
import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service

@Service
class CourseServiceImpl(
    private val courseReader: CourseReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
) : CourseService {
    override fun findAll(query: FilterCoursesQuery): List<CourseResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)

        val courses = if (query.field != null) {
            courseReader.findAllInCategory(
                query.category,
                courseCodes,
                query.field,
                query.schoolId
            )
        } else {
            courseReader.findAllInCategory(
                query.category,
                courseCodes,
                query.schoolId
            )
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

    @Deprecated("Use findAll with FilterCoursesQuery instead")
    fun findAll(query: MajorRequiredCourseQuery): List<MajorRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllInCategory(Category.MAJOR_REQUIRED, courseCodes, query.schoolId)
        return courses.map { MajorRequiredResponse.from(it) }
    }

    @Deprecated("Use findAll with FilterCoursesQuery instead")
    fun findAll(query: MajorElectiveCourseQuery): List<MajorElectiveResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        val courses = courseReader.findAllInCategory(Category.MAJOR_ELECTIVE, courseCodes, query.schoolId)
        return courses.map { MajorElectiveResponse.from(it) }
    }

    @Deprecated("Use findAll with FilterCoursesQuery instead")
    fun findAll(query: GeneralRequiredCourseQuery): List<GeneralRequiredResponse> {
        val department = departmentReader.getByName(query.departmentName)
        val courseCodes = targetReader.findAllByDepartmentGrade(department, query.grade)
        if (query.field == null) {
            val courses = courseReader.findAllInCategory(Category.GENERAL_REQUIRED, courseCodes, query.schoolId)
            return courses.map { GeneralRequiredResponse.from(it) }
        }
        val courses = courseReader.findAllInCategory(
            Category.GENERAL_REQUIRED,
            courseCodes,
            query.field,
            query.schoolId
        )
        return courses.map { GeneralRequiredResponse.from(it) }
    }
}
