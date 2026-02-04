package com.yourssu.soongpt.domain.courseTime.storage

import jakarta.persistence.*

@Entity
@Table(name = "course_time")
class CourseTimeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val courseCode: Long,

    @Column(nullable = false)
    val dayOfWeek: String,

    @Column(nullable = false)
    val startMinute: Int,

    @Column(nullable = false)
    val endMinute: Int,

    @Column(nullable = true)
    val room: String? = null,
)
