package com.yourssu.soongpt.message.storage.exception

import com.yourssu.soongpt.common.handler.NotFoundException

class MessageNotFoundException : NotFoundException(message = "해당하는 메세지가 없습니다.")