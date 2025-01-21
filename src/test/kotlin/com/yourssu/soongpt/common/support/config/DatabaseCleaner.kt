package com.yourssu.soongpt.common.support.config

import com.google.common.base.CaseFormat
import jakarta.persistence.Entity
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.persistence.Table
import jakarta.persistence.metamodel.EntityType
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

@Component
class DatabaseCleaner : InitializingBean {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private var tableNames: List<String>? = null

    override fun afterPropertiesSet() {
        tableNames = entityManager.metamodel.entities.stream()
            .filter({ e -> e.javaType.getAnnotation(Entity::class.java) != null })
            .map(this::extractTableName)
            .map({ tableName -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, tableName) })
            .collect(Collectors.toList())
    }

    private fun extractTableName(e: EntityType<*>): String {
        val tableAnnotation = e.javaType.getAnnotation(Table::class.java)
        return tableAnnotation?.name ?: e.name
    }

    @Transactional
    fun execute() {
        entityManager.flush()
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
        for (tableName in tableNames!!) {
            truncateTableForId(tableName)
        }
        entityManager.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }

    private fun truncateTableForId(tableName: String) {
        entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
        entityManager.createNativeQuery(
            "ALTER TABLE " + tableName + " ALTER COLUMN id RESTART WITH 1"
        ).executeUpdate()
    }

    private fun truncateTableForTableId(tableName: String) {
        entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
        entityManager.createNativeQuery(
            "ALTER TABLE " + tableName + " ALTER COLUMN " + tableName.replace("\"", "") + "_id RESTART WITH 1"
        ).executeUpdate()
    }
}
