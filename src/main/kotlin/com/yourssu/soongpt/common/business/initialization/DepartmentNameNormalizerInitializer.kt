package com.yourssu.soongpt.common.business.initialization

import com.yourssu.soongpt.common.config.CollegeProperties
import com.yourssu.soongpt.common.util.DepartmentNameNormalizer
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class DepartmentNameNormalizerInitializer(
    private val collegeProperties: CollegeProperties,
) {
    @PostConstruct
    fun initialize() {
        val canonicalDepartments = collegeProperties.colleges
            .flatMap { it.departments }
            .toSet()

        val aliases = collegeProperties.departmentNormalization.aliases

        DepartmentNameNormalizer.initialize(
            canonicalDepartments = canonicalDepartments,
            aliases = aliases,
        )
    }
}
