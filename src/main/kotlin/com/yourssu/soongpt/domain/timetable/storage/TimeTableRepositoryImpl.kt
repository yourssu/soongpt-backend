package com.yourssu.soongpt.domain.timetable.storage

import com.yourssu.soongpt.domain.timetable.implement.Timetable
import com.yourssu.soongpt.domain.timetable.implement.TimetableRepository
import com.yourssu.soongpt.domain.timetable.storage.exception.TimetableNotFoundException
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

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
}

interface TimetableJpaRepository: JpaRepository<TimetableEntity, Long> {
}