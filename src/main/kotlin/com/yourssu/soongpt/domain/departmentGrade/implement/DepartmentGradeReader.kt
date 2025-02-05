package com.yourssu.soongpt.domain.departmentGrade.implement

import com.yourssu.soongpt.domain.course.business.ParsedTarget
import com.yourssu.soongpt.domain.department.implement.DepartmentReader
import org.springframework.stereotype.Component

@Component
class DepartmentGradeReader(
    private val departmentGradeRepository: DepartmentGradeRepository,
    private val departmentReader: DepartmentReader
) {
    fun getByDepartmentIdAndGrade(departmentId: Long, grade: Int): DepartmentGrade {
        return departmentGradeRepository.getByDepartmentIdAndGrade(departmentId, grade)
    }

    fun getAll(): List<DepartmentGrade> {
        return departmentGradeRepository.getAll()
    }

    fun getByDepartmentId(departmentId: Long): List<DepartmentGrade> {
        return departmentGradeRepository.getByDepartmentId(departmentId)
    }

    fun getByGrade(grade: Int): List<DepartmentGrade> {
        return departmentGradeRepository.getByGrade(grade)
    }

    fun getByDepartmentIdsAndGrades(parsedTarget: ParsedTarget): List<DepartmentGrade> {
        // 제외 대상 학과 이름에 해당하는 학과 id 집합을 미리 계산(없으면 빈 집합)
        val excludeDeptIds: Set<Long> = if (parsedTarget.excludedDepartments.isNotEmpty()) {
            parsedTarget.excludedDepartments
                .flatMap { ex ->
                    departmentReader.getMatchingDepartments(ex)
                        .mapNotNull { it.id }
                }
                .toSet()
        } else {
            emptySet()
        }

        // 케이스별로 제외 대상을 고려하지 않은 디폴트 학과 목록을 계산
        val baseList: List<DepartmentGrade> = when {
            // CASE A: 전체학년, 전체학과
            parsedTarget.grade == 0 && parsedTarget.includedDepartments.contains("전체") ->
                getAll()
            // CASE B: 전체학년, 특정학과
            parsedTarget.grade == 0 ->
                parsedTarget.includedDepartments.flatMap { deptName ->
                    val departments = departmentReader.getMatchingDepartments(deptName)
                    departments.flatMap { department ->
                        getByDepartmentId(department.id!!)
                    }
                }
            // CASE B: 특정학년, 전체학과
            parsedTarget.grade != 0 && parsedTarget.includedDepartments.contains("전체") ->
                getByGrade(parsedTarget.grade)
            // CASE D: 특정학년, 특정학과
            else ->
                parsedTarget.includedDepartments.flatMap { deptName ->
                    val departments = departmentReader.getMatchingDepartments(deptName)
                    departments.mapNotNull { department ->
                        getByDepartmentIdAndGrade(department.id!!, parsedTarget.grade)
                    }
                }
        }

        // 제외 대상 학과 ID가 있다면 필터링
        val filteredList = if (excludeDeptIds.isNotEmpty()) {
            baseList.filterNot { dg -> dg.departmentId in excludeDeptIds }
        } else {
            baseList
        }

        return filteredList.distinctBy { it.id!! }
    }
}