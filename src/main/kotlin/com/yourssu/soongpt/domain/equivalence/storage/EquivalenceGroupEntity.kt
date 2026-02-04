package com.yourssu.soongpt.domain.equivalence.storage

import com.yourssu.soongpt.common.entity.BaseEntity
import com.yourssu.soongpt.domain.equivalence.implement.EquivalenceGroup
import jakarta.persistence.*

@Entity
@Table(name = "equivalence_group")
class EquivalenceGroupEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = true)
    val name: String? = null,

    @Column(nullable = true, columnDefinition = "TEXT")
    val description: String? = null,
) : BaseEntity() {
    companion object {
        fun from(group: EquivalenceGroup) = EquivalenceGroupEntity(
            id = group.id,
            name = group.name,
            description = group.description,
        )
    }

    fun toDomain() = EquivalenceGroup(
        id = id,
        name = name,
        description = description,
    )
}
