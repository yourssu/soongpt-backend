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
    private lateinit var em: EntityManager

    private var tableNames: List<String> = ArrayList()

    override fun afterPropertiesSet() {
        tableNames = em.metamodel.entities.stream()
            .filter({ e -> e.javaType.getAnnotation(Entity::class.java) != null })
            .map(this::extractTableName)
            .map { tableName -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, tableName) }
            .collect(Collectors.toList())
    }

    private fun extractTableName(e: EntityType<*>): String {
        val tableAnnotation = e.javaType.getAnnotation(Table::class.java)
        return tableAnnotation?.name ?: e.name
    }

    @Transactional
    fun execute() {
        em.flush()
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY FALSE").executeUpdate()
        for (tableName in tableNames) {
            em.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
        }
        resetIdentityColumns()
        em.createNativeQuery("SET REFERENTIAL_INTEGRITY TRUE").executeUpdate()
    }

    fun resetIdentityColumns() {
        for ((tableName, columnName) in findIdentities()) {
            val alterQuery = "ALTER TABLE $tableName ALTER COLUMN $columnName RESTART WITH 1"
            em.createNativeQuery(alterQuery).executeUpdate()
        }
    }

    private fun findIdentities(): List<Array<*>> {
        val query = """
                SELECT table_name, column_name 
                FROM information_schema.columns 
                WHERE is_identity = 'YES'
            """.trimIndent()
        return em.createNativeQuery(query).resultList
            .map { it as Array<*> }
    }
}
