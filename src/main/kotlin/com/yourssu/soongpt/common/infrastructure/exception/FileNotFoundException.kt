package com.yourssu.soongpt.common.infrastructure.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class FileNotFoundException(filePath: String = ""): NotFoundException(message = "파일이 존재하지 않습니다. 파일 경로: $filePath") {
}
