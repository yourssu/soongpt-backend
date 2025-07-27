package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.Category
import com.yourssu.soongpt.domain.course.implement.Course
import jakarta.persistence.*

@Entity
@Table(name = "course")
class CourseEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: Category,

    @Column(nullable = true)
    val subCategory: String? = null,

    @Column(nullable = false)
    val field: String? = null,

    @Column(nullable = false, unique = true)
    val code: Long,

    @Column(nullable = false)
    val name: String,

    @Column(nullable = true)
    val professor: String? = null,

    @Column(nullable = false)
    val department: String,

    @Column(nullable = true)
    val division: String? = null,

    @Column(nullable = false)
    val time: String,

    @Column(nullable = false)
    val point: String,

    @Column(nullable = false)
    val personeel: Int,

    @Column(nullable = false)
    val scheduleRoom: String,

    @Column(nullable = false)
    val target: String,
)