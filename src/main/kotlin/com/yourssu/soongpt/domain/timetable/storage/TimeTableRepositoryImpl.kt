package com.yourssu.soongpt.domain.timetable.storage

import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.Timetable
import com.yourssu.soongpt.domain.timetable.implement.TimetableRepository
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import kotlin.random.Random

@Repository
class TimetableRepositoryImpl(
    private val timetableJpaRepository: TimetableJpaRepository,
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
        return timetableJpaRepository.findRandomNative()?.toDomain()
    }

    override fun findRandomTimetable(): Timetable? {
        return findRandom()
    }
}

interface TimetableJpaRepository: JpaRepository<TimetableEntity, Long> {

    @org.springframework.data.jpa.repository.Query(
        value = "select * from timetable order by rand() limit 1",
        nativeQuery = true
    )
    fun findRandomNative(): TimetableEntity?

}
