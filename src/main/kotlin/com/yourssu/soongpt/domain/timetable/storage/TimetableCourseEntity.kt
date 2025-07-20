package com.yourssu.soongpt.domain.timetable.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.timetable.implement.TimetableCourse
import jakarta.persistence.*

@Entity
@Table(name = "timetable_course")
class TimetableCourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val timetableId: Long,

    @Column(nullable = false)
    val courseId: Long,
) : BaseEntity() {
    companion object {
        fun from(timetableCourse: TimetableCourse): TimetableCourseEntity {
            return TimetableCourseEntity(
                id = timetableCourse.id,
                timetableId = timetableCourse.timetableId,
                courseId = timetableCourse.courseId
            )
        }
    }

    fun toDomain(): TimetableCourse {
        return TimetableCourse(
            id = id,
            timetableId = timetableId,
            courseId = courseId
        )
    }
}