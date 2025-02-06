package com.yourssu.soongpt.domain.course.implement.exception

import com.yourssu.soongpt.common.handler.BadRequestException

class ViolatedTotalCreditRuleException(total: String = "") : BadRequestException(message = "최대 학점은 22학점입니다. <<입력한 학점: $total>>") {

}