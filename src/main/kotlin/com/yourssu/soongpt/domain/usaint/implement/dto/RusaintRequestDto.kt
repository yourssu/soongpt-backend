package com.yourssu.soongpt.domain.usaint.implement.dto

import com.fasterxml.jackson.annotation.JsonProperty

/** rusaint-service snapshot API 요청. Python UsaintSnapshotRequest와 필드명 일치. */
data class RusaintSyncRequest(
    val studentId: String,
    @get:JsonProperty("sToken") val sToken: String,
)
