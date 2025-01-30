package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "ssu-data")
data class CollegeProperties(
    val colleges: List<CollegeInfo>,
) {
    data class CollegeInfo(
        val name: String,
        val departments: List<String>,
    )
}