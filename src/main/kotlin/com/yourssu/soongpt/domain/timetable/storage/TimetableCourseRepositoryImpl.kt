package com.yourssu.soongpt.domain.timetable.storage

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourse
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourseRepository
import com.yourssu.soongpt.domain.timetable.storage.QTimetableCourseEntity.timetableCourseEntity
import com.yourssu.soongpt.domain.timetable.storage.QTimetableEntity.timetableEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
class TimetableCourseRepositoryImpl(
    private val timetableCourseJpaRepository: TimetableCourseJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
): TimetableCourseRepository {
    override fun save(timetableCourse: TimetableCourse): TimetableCourse {
        return timetableCourseJpaRepository.save(TimetableCourseEntity.from(timetableCourse))
            .toDomain()
    }

    override fun saveAll(timetableCourses: List<TimetableCourse>): List<TimetableCourse> {
        if (timetableCourses.isEmpty()) return emptyList()
        return timetableCourseJpaRepository.saveAll(
            timetableCourses.map { TimetableCourseEntity.from(it) }
        ).map { it.toDomain() }
    }

    override fun findAllCourseByTimetableId(timetableId: Long): List<TimetableCourse> {
        return jpaQueryFactory.selectFrom(timetableCourseEntity)
            .join(timetableEntity)
            .on(timetableCourseEntity.timetableId.eq(timetableEntity.id))
            .where(timetableEntity.id.eq(timetableId))
            .fetch()
            .map { it.toDomain() }
    }
}

interface TimetableCourseJpaRepository: JpaRepository<TimetableCourseEntity, Long> {
}
