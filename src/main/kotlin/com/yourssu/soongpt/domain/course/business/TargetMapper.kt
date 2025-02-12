package com.yourssu.soongpt.domain.course.business

import jakarta.annotation.PostConstruct
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component

@Component
class TargetMapper {
    private lateinit var targetMap: Map<String, String>
    private lateinit var classificationMap: Map<String, String>

    @PostConstruct
    fun initMapping() {
        targetMap = createMapping("수강분류_가공_전.json", "수강분류_가공_후.json")
        classificationMap = createMapping("이수구분_가공_전.json", "이수구분_가공_후.json")
    }

    private fun createMapping(beforeFile: String, afterFile: String): Map<String, String> {
        val rawBefore = readJsonFile(beforeFile)
        val rawAfter = readJsonFile(afterFile)

        val beforeList: List<String> = Json.decodeFromString(rawBefore)
        val afterList: List<String> = Json.decodeFromString(rawAfter)

        return beforeList.zip(afterList).toMap()
    }

    private fun readJsonFile(fileName: String): String {
        return this::class.java.classLoader
            .getResource(fileName)
            ?.readText()
            ?: "[]"
    }

    fun getMappedTarget(beforeTarget: String): String? {
        return targetMap[beforeTarget]
    }

    fun getMappedClassification(beforeTarget: String): String? {
        return classificationMap[beforeTarget]
    }
}