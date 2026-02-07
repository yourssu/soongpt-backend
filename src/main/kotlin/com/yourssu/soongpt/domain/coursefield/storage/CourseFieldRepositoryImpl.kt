package com.yourssu.soongpt.domain.coursefield.storage

import com.yourssu.soongpt.domain.coursefield.implement.CourseField
import com.yourssu.soongpt.domain.coursefield.implement.CourseFieldRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component

@Component
class CourseFieldRepositoryImpl(
    private val courseFieldJpaRepository: CourseFieldJpaRepository,
) : CourseFieldRepository {
    override fun findByCourseCode(courseCode: Long): CourseField? {
        return courseFieldJpaRepository.findByCourseCode(courseCode)?.toDomain()
    }

    override fun findAll(): List<CourseField> {
        return courseFieldJpaRepository.findAll().map { it.toDomain() }
    }

    override fun saveAll(courseFields: List<CourseField>): List<CourseField> {
        val entities = courseFields.map { CourseFieldEntity.from(it) }
        return courseFieldJpaRepository.saveAll(entities).map { it.toDomain() }
    }

    override fun deleteAll() {
        courseFieldJpaRepository.deleteAll()
    }
}

interface CourseFieldJpaRepository : JpaRepository<CourseFieldEntity, Long> {
    fun findByCourseCode(courseCode: Long): CourseFieldEntity?
}
