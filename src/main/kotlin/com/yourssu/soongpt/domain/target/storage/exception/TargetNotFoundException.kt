package com.yourssu.soongpt.domain.target.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class TargetNotFoundException : NotFoundException(message = "수강 대상을 찾을 수 없습니다.")