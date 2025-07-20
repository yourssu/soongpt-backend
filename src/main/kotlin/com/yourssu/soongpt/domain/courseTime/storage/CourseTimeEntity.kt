package com.yourssu.soongpt.domain.courseTime.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.courseTime.implement.CourseTime
import com.yourssu.soongpt.domain.courseTime.implement.Time
import com.yourssu.soongpt.domain.courseTime.implement.Week
import jakarta.persistence.*

@Entity
@Table(name = "course_time")
class CourseTimeEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val week: Week,

    @Column(nullable = false)
    val startTime: Int,

    @Column(nullable = false)
    val endTime: Int,

    @Column(nullable = true)
    val classroom: String?,

    @Column(nullable = false)
    val courseId: Long,
    ) : BaseEntity() {
    companion object {
        fun from(courseTime: CourseTime) = CourseTimeEntity(
            id = courseTime.id,
            week = courseTime.week,
            startTime = courseTime.startTime.time,
            endTime = courseTime.endTime.time,
            classroom = courseTime.classroom,
            courseId = courseTime.courseId,
        )
    }

    fun toDomain() = CourseTime(
        id = id,
        week = week,
        startTime = Time(startTime),
        endTime = Time(endTime),
        classroom = classroom,
        courseId = courseId,
    )
}
