package com.yourssu.soongpt.domain.timetable.storage

import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.Timetable
import com.yourssu.soongpt.domain.timetable.implement.TimetableRepository
import com.yourssu.soongpt.domain.timetable.storage.QTimetableEntity.timetableEntity
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.random.Random

@Repository
class TimetableRepositoryImpl(
    private val timetableJpaRepository: TimetableJpaRepository,
    private val jpaQueryFactory: JPAQueryFactory,
): TimetableRepository {
    override fun save(timetable: Timetable): Timetable {
        return timetableJpaRepository.save(TimetableEntity.from(timetable))
            .toDomain()
    }

    override fun get(id: Long): Timetable {
        return timetableJpaRepository.findById(id)
            .orElseThrow { TimetableNotFoundException() }
            .toDomain()
    }

    override fun delete(id: Long) {
        if (!timetableJpaRepository.existsById(id)) {
            throw TimetableNotFoundException()
        }
        timetableJpaRepository.deleteById(id)
    }

    override fun count(): Long {
        return timetableJpaRepository.count()
    }

    override fun findRandom(): Timetable? {
        val randomOrder = Expressions.numberTemplate(Double::class.java, "function('RAND')")
        return jpaQueryFactory
            .selectFrom(timetableEntity)
            .orderBy(randomOrder.asc())
            .limit(1)
            .fetchOne()
            ?.toDomain()
    }


}

interface TimetableJpaRepository: JpaRepository<TimetableEntity, Long> {

}
