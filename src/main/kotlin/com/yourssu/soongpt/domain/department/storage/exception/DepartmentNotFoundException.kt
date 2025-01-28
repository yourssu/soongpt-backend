package com.yourssu.soongpt.domain.department.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class DepartmentNotFoundException : NotFoundException(message = "해당하는 학과가 없습니다.")
