package com.yourssu.soongpt.domain.departmentGrade.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class DepartmentGradeNotFoundException : NotFoundException(message = "해당하는 학과/학년이 없습니다.")