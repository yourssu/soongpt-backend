package com.yourssu.soongpt.domain.timetable.storage

import com.yourssu.soongpt.domain.timetable.implement.Tag
import com.yourssu.soongpt.domain.timetable.implement.Timetable
import jakarta.persistence.*

@Entity
@Table(name = "timetable")
class TimetableEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val tag: Tag,

    @Column(nullable = false)
    val score: Int = 0
) {
    companion object {
        fun from(timetable: Timetable): TimetableEntity {
            return TimetableEntity(
                id = timetable.id,
                tag = timetable.tag,
                score = timetable.score
            )
        }
    }

    fun toDomain(): Timetable {
        return Timetable(
            id = id,
            tag = tag,
            score = score
        )
    }
}