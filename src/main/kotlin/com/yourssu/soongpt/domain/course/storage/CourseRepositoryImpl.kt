package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.Course
import com.yourssu.soongpt.domain.course.implement.CourseRepository
import com.yourssu.soongpt.domain.course.storage.exception.CourseNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class CourseRepositoryImpl(
    private val courseJpaRepository: CourseJpaRepository,
) : CourseRepository {
    override fun save(course: Course): Course {
        return courseJpaRepository.save(CourseEntity.from(course))
            .toDomain()
    }

    override fun get(id: Long): Course {
        val course = courseJpaRepository.findByIdOrNull(id)
            ?: throw CourseNotFoundException()
        return course.toDomain()
    }
}

interface CourseJpaRepository : JpaRepository<CourseEntity, Long> {
}