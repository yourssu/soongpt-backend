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
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimes
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldReader
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.target.implement.TargetReader
import org.springframework.stereotype.Service

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
                                userGrade = grade,
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
                                userGrade = grade,
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
