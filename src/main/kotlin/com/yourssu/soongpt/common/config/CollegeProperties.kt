package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ssu-data")
data class CollegeProperties(
    val colleges: List<CollegeInfo>,
    val departmentNormalization: DepartmentNormalization = DepartmentNormalization(),
) {
    data class CollegeInfo(
        val name: String,
        val departments: List<String>,
    )

    data class DepartmentNormalization(
        val aliases: Map<String, String> = emptyMap(),
    )
}
