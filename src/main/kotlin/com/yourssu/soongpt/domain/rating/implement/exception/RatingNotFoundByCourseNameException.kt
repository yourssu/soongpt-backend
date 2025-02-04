package com.yourssu.soongpt.domain.rating.implement.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class RatingNotFoundByCourseNameException() : NotFoundException(message = "과목 이름 포함되고 교수 이름이 일치하는 만족도 조사 정보를 찾을 수 없습니다.")
