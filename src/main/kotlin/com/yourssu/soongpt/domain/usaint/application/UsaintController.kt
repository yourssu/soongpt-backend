package com.yourssu.soongpt.domain.usaint.application

import com.yourssu.soongpt.common.business.dto.Response
import com.yourssu.soongpt.domain.usaint.application.dto.UsaintSyncRequest
import com.yourssu.soongpt.domain.usaint.business.UsaintService
import com.yourssu.soongpt.domain.usaint.business.dto.UsaintSyncResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/usaint")
class UsaintController(
    private val usaintService: UsaintService,
) {

    /**
     * 클라이언트에서 학번(studentId)과 sToken을 받아
     * 내부 rusaint-service로 전달해 u-saint 정보를 동기화하는 엔드포인트.
     *
     * 이후 단계에서:
     * - rusaint-service 응답을 기반으로 DB 저장
     * - Redis 캐싱
     * 등을 확장할 수 있습니다.
     */
    @PostMapping("/sync")
    fun syncTimetable(
        @RequestBody request: UsaintSyncRequest,
    ): ResponseEntity<Response<UsaintSyncResponse>> {
        val response = usaintService.syncUsaintData(request)
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(Response(result = response))
    }
}
