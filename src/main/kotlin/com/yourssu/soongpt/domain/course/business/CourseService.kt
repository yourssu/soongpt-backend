package com.yourssu.soongpt.domain.course.business

import com.yourssu.soongpt.domain.course.application.dto.CreateCourseRequest
import com.yourssu.soongpt.domain.course.business.dto.CourseResponse
import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseReader
import com.yourssu.soongpt.domain.course.implement.CourseWriter
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeReader
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeWriter
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeReader
import com.yourssu.soongpt.domain.target.implement.Target
import com.yourssu.soongpt.domain.target.implement.TargetReader
import com.yourssu.soongpt.domain.target.implement.TargetWriter
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CourseService(
    private val courseReader: CourseReader,
    private val courseTimeReader: CourseTimeReader,
    private val departmentReader: DepartmentReader,
    private val targetReader: TargetReader,
    private val courseParser: CourseParser,
    private val courseWriter: CourseWriter,
    private val courseTimeWriter: CourseTimeWriter,
    private val departmentGradeReader: DepartmentGradeReader,
    private val targetWriter: TargetWriter,
    private val targetMapper: TargetMapper
) {
    @Cacheable(value = ["courseCache"], key = "'majorRequired'.concat(#command.departmentName).concat(#command.grade)")
    fun findByDepartmentNameInMajorRequired(command: FoundDepartmentCommand): List<CourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeIdInMajorRequired(departmentGrade.id!!)
        return filterNotEmptyCourseTimes(courses.map {
            val courseTimes = courseTimeReader.findAllByCourseId(it.id!!)
            CourseResponse.from(
                course = it,
                target = listOf(targetReader.formatTargetDisplayName(department, departmentGrade)),
                courseTimes = courseTimes
            )
        })
    }

    @Cacheable(value = ["courseCache"], key = "'MajorElective'.concat(#command.departmentName).concat(#command.grade)")
    fun findByDepartmentNameInMajorElective(command: FoundDepartmentCommand): List<CourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val courses = courseReader.findAllByDepartmentIdInMajorElective(department.id!!)
        return filterNotEmptyCourseTimes(courses.map { (course, departmentGrades) ->
            val courseTimes = courseTimeReader.findAllByCourseId(course.id!!)
            CourseResponse.from(
                course = course,
                target = departmentGrades.map { targetReader.formatTargetDisplayName(department, it) },
                courseTimes = courseTimes)
        })
    }

    @Cacheable(value = ["courseCache"], key = "'GeneralRequired'.concat(#command.departmentName).concat(#command.grade)")
    fun findByDepartmentNameInGeneralRequired(command: FoundDepartmentCommand): List<CourseResponse> {
        val department = departmentReader.getByName(command.departmentName)
        val departmentGrade = departmentGradeReader.getByDepartmentIdAndGrade(department.id!!, command.grade)
        val courses = courseReader.findAllByDepartmentGradeIdInGeneralRequired(departmentGrade.id!!)
        return filterNotEmptyCourseTimes(courses.map {
            val courseTimes = courseTimeReader.findAllByCourseId(it.id!!)
            CourseResponse.from(
                course = it,
                target = listOf(targetReader.formatTargetDisplayName(department, departmentGrade)),
                courseTimes = courseTimes
            )
        })
    }

    private fun filterNotEmptyCourseTimes(courses: List<CourseResponse>): List<CourseResponse> {
        return courses.filter { it.courseTime.isNotEmpty() }
    }

    @Transactional
    fun createCourses(courses: List<CreateCourseRequest>) {
        courses.forEach { course ->
            try {
                val classification =
                    course.category?.let { targetMapper.getMappedClassification(it) } ?: throw IllegalArgumentException(
                        "변환한 이수구분" +
                                "이 null임, 원본 이수구분 : ${course.category.orEmpty()}"
                    )
                val parsedClassifications = courseParser.parseClassifications(classification)
                if (parsedClassifications.isEmpty()) {
                    println("허용되지 않는 이수구분 : ${course.category.orEmpty()}")
                    return@forEach
                }

                val professorName = courseParser.parseProfessorNames(course.professor)
                val credit = courseParser.parseCredit(course.time_points)

                val createdCourses = mutableListOf<Course>()
                val classificationToCourse = mutableMapOf<Classification, Course>()
                parsedClassifications.forEach { (classification, _) ->
                    val courseDomain = Course(
                        courseName = course.name,
                        professorName = professorName,
                        classification = classification,
                        credit = credit
                    )
                    val savedCourse = courseWriter.save(courseDomain)
                    createdCourses.add(savedCourse)
                    classificationToCourse[classification] = savedCourse
                }

                createdCourses.forEach { courseDomain ->
                    val courseTimes = courseParser.parseCourseTimes(course.schedule_room.orEmpty(), courseDomain.id!!)
                    courseTimes.forEach { courseTime ->
                        courseTimeWriter.save(courseTime)
                    }
                }

                val target = course.target?.let { targetMapper.getMappedTarget(it) } ?: throw IllegalArgumentException(
                    "변환한 타겟" +
                            "이 null임, 원본 타겟 : ${course.target.orEmpty()}"
                )
                val parsedTargets = courseParser.parseTarget(target)
                parsedTargets.forEach { parsedTarget ->
                    val deptGrades = departmentGradeReader.getByDepartmentIdsAndGrades(parsedTarget)
                    deptGrades.forEach { deptGrade ->
                        val deptId = deptGrade.departmentId
                        val matchedClassification = if (parsedClassifications.size == 1) {
                            parsedClassifications.keys.first()
                        } else {
                            parsedClassifications.entries.find { (_, deptIds) ->
                                if (deptIds.isEmpty()) true else deptIds.contains(deptId)
                            }?.key ?: throw IllegalArgumentException(
                                "이수구분 매칭이 되지 않은 타겟 : 이수구분 - ${course.category.orEmpty()}, 타겟 학과 ID -" +
                                        " ${deptId}"
                            )
                        }
                        val assignedCourseId = classificationToCourse[matchedClassification]?.id
                            ?: throw IllegalStateException("매칭된 이수구분-${matchedClassification}에 해당하는 과목이 존재하지 않음")
                        val targetDomain = Target(
                            departmentGradeId = deptGrade.id!!,
                            courseId = assignedCourseId
                        )
                        targetWriter.save(targetDomain)
                    }
                }
            } catch (e: Exception) {
                println("예외 발생 : ${course.name} - ${e.message}")
            }
        }
    }

    fun findById(courseId: Long): CourseResponse {
        val course = courseReader.getById(courseId)
        val courseTimes = courseTimeReader.findAllByCourseId(courseId)
        val target = targetReader.findAllByCourseId(courseId)
        return CourseResponse.from(course, target, courseTimes)
    }
}
