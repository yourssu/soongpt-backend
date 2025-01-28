package com.yourssu.soongpt.domain.department.implement.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class DepartmentNotFoundByNameException: NotFoundException(message = "입력한 학과명과 일치하는 학과가 없습니다.") {
}