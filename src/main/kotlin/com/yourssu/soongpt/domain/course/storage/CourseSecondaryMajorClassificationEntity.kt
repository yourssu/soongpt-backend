package com.yourssu.soongpt.domain.course.storage

import com.yourssu.soongpt.domain.course.implement.SecondaryMajorCompletionType
import com.yourssu.soongpt.domain.course.implement.SecondaryMajorTrackType
import jakarta.persistence.*

@Entity
@Table(
    name = "course_secondary_major_classification",
    indexes = [
        Index(
            name = "idx_csmc_lookup",
            columnList = "track_type,completion_type,department_id,course_code",
        ),
        Index(name = "idx_csmc_course_code", columnList = "course_code"),
    ],
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_csmc_course_track_completion_department",
            columnNames = ["course_code", "track_type", "completion_type", "department_id"],
        ),
    ],
)
class CourseSecondaryMajorClassificationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val courseCode: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val trackType: SecondaryMajorTrackType,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val completionType: SecondaryMajorCompletionType,

    @Column(nullable = false)
    val departmentId: Long,

    @Column(nullable = false, length = 64)
    val rawClassification: String,

    @Column(nullable = false, length = 100)
    val rawDepartmentToken: String,
)
