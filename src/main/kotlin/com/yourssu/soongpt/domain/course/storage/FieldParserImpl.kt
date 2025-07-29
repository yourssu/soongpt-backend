package com.yourssu.soongpt.domain.course.storage

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.yourssu.soongpt.domain.course.implement.FieldListFinder
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.IOException

@Component
object FieldParserImpl: FieldListFinder {
    private val objectMapper = ObjectMapper().registerModule(kotlinModule())
    private val fieldsData: Map<String, List<String>> by lazy {
        loadFieldsData()
    }

    private fun getFieldsByKey(key: String): List<String> {
        return fieldsData[key] ?: emptyList()
    }

    private fun getAllKeys(): Set<String> {
        return fieldsData.keys
    }

    private fun loadFieldsData(): Map<String, List<String>> {
        return try {
            val resource = ClassPathResource("result/2025_2/fields/fields.json")
            objectMapper.readValue(
                resource.inputStream,
                objectMapper.typeFactory.constructMapType(Map::class.java, String::class.java, List::class.java)
            )
        } catch (e: IOException) {
            emptyMap()
        }
    }

    override fun getFieldsBySchoolId(schoolId: Int): List<String> {
        return getFieldsByKey(schoolId.toString())
    }
}
