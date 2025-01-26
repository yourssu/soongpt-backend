package com.yourssu.soongpt.domain.courseTime.storage

import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.CourseTimeRepository
import com.yourssu.soongpt.domain.courseTime.storage.exception.CourseTimeNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class CourseTimeRepositoryImpl(
        private val courseTimeJpaRepository: CourseTimeJpaRepository
) : CourseTimeRepository {
    override fun save(courseTime: CourseTime): CourseTime {
        return courseTimeJpaRepository.save(CourseTimeEntity.from(courseTime))
            .toDomain()
    }

    override fun get(id: Long): CourseTime {
        val courseTime =  courseTimeJpaRepository.findByIdOrNull(id)
            ?: throw CourseTimeNotFoundException()
        return courseTime.toDomain()
    }
}

interface CourseTimeJpaRepository: JpaRepository<CourseTimeEntity, Long>
