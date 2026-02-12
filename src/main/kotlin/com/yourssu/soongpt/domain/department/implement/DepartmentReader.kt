package com.yourssu.soongpt.domain.department.implement

import com.yourssu.soongpt.common.util.DepartmentNameNormalizer
import org.springframework.stereotype.Component

@Component
class DepartmentReader(
    private val departmentRepository: DepartmentRepository,
) {
    fun getByName(name: String): Department {
        val normalizedName = DepartmentNameNormalizer.normalize(name)
        return departmentRepository.getByName(normalizedName)
    }

    fun get(id: Long): Department {
        return departmentRepository.get(id)
    }
}
