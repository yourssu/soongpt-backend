package com.yourssu.soongpt.domain.college.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class CollegeNotFoundException : NotFoundException(message = "해당하는 단과대가 없습니다.")