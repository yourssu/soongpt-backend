package com.yourssu.soongpt.domain.course

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.course.QCourseEntity.courseEntity
import com.yourssu.soongpt.domain.course.implement.Classification
import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.implement.Courses
import com.yourssu.soongpt.domain.departmentGrade.storage.QDepartmentGradeEntity.departmentGradeEntity
import com.yourssu.soongpt.domain.target.storage.QTargetEntity.targetEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class CourseRepositoryImpl(
    private val courseJpaRepository: CourseJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
) : CourseRepository {
    override fun save(course: Course): Course {
        return courseJpaRepository.save(CourseEntity.from(course))
            .toDomain()
    }

    override fun findAllByDepartmentId(departmentId: Long, classification: Classification): List<Course> {
        return jpaQueryFactory.selectFrom(courseEntity)
            .innerJoin(targetEntity)
            .on(courseEntity.id.eq(targetEntity.courseId))
            .innerJoin(departmentGradeEntity)
            .on(targetEntity.departmentGradeId.eq(departmentGradeEntity.id))
            .where(
                departmentGradeEntity.departmentId.eq(departmentId),
                courseEntity.classification.eq(classification)
            )
            .fetch()
            .map { it.toDomain() }
    }

    override fun getAll(ids: List<Long>): List<Course> {
        return jpaQueryFactory.selectFrom(courseEntity)
            .where(courseEntity.id.`in`(ids))
            .fetch()
            .map { it.toDomain() }
    }

    override fun findByDepartmentIdAndCourseName(
        departmentId: Long,
        courseName: String,
        classification: Classification
    ): Courses {
        val courses = jpaQueryFactory.selectFrom(courseEntity)
            .innerJoin(targetEntity)
            .on(courseEntity.id.eq(targetEntity.courseId))
            .innerJoin(departmentGradeEntity)
            .on(targetEntity.departmentGradeId.eq(departmentGradeEntity.id))
            .where(
                departmentGradeEntity.departmentId.eq(departmentId),
                courseEntity.courseName.eq(courseName),
                courseEntity.classification.eq(classification)
            )
            .fetch()
            .map { it.toDomain() }
        return Courses(courses)
    }
}

interface CourseJpaRepository : JpaRepository<CourseEntity, Long> {
}