package com.yourssu.soongpt.domain.timetable.storage

import com.yourssu.soongpt.domain.timetable.implement.Timetable
import com.yourssu.soongpt.domain.timetable.implement.TimetableRepository
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
        return timetableJpaRepository.findById(id).get().toDomain()
    }
}

interface TimetableJpaRepository: JpaRepository<TimetableEntity, Long> {
}