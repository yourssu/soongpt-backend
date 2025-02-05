package com.yourssu.soongpt.common.business.initialization

import com.querydsl.jpa.impl.JPAQueryFactory
import com.yourssu.soongpt.common.config.CollegeProperties
import com.yourssu.soongpt.domain.college.implement.College
import com.yourssu.soongpt.domain.college.implement.CollegeWriter
import com.yourssu.soongpt.domain.college.storage.QCollegeEntity.collegeEntity
import com.yourssu.soongpt.domain.department.implement.Department
import com.yourssu.soongpt.domain.department.implement.DepartmentWriter
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGrade
import com.yourssu.soongpt.domain.departmentGrade.implement.DepartmentGradeWriter
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class CollegesAndDepartmentsInitializer(
    private val collegeWriter: CollegeWriter,
    private val departmentWriter: DepartmentWriter,
    private val departmentGradeWriter: DepartmentGradeWriter,
    private val jpaQueryFactory: JPAQueryFactory,
    private val properties: CollegeProperties,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        if (alreadyInitialized()) {
            return
        }
        for ((collegeName, departmentNames) in properties.colleges) {
            initialize(collegeName, departmentNames)
        }
    }

    private fun alreadyInitialized(): Boolean {
        return jpaQueryFactory.select(collegeEntity.count())
            .from(collegeEntity).fetchOne() != 0L
    }

    private fun initialize(collegeName: String, departments: List<String>) {
        val college: College = collegeWriter.save(College(name = collegeName))
        val department = departmentWriter.saveAll(departments.map { Department(collegeId = college.id!!, name = it) })
        initializeDepartmentGrades(department)
    }

    private fun initializeDepartmentGrades(departments: List<Department>) {
        for (grade in 1..5) {
            departmentGradeWriter.saveAll(departments.map { DepartmentGrade(departmentId = it.id!!, grade = grade) })
        }
    }
}
