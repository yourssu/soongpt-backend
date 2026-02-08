package com.yourssu.soongpt.domain.usaint.implement

import com.yourssu.soongpt.common.config.RusaintProperties
import org.springframework.stereotype.Component
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 학번(studentId)을 기반으로 HMAC-SHA256 pseudonym을 생성합니다.
 *
 * - 동일 학번에 대해 항상 동일한 pseudonym이 생성되도록 함
 * - 실제 키 값은 YAML 설정(rusaint.pseudonym-secret)에서 관리
 */
@Component
class PseudonymGenerator(
    private val rusaintProperties: RusaintProperties,
) {
    init {
        val secretSize = rusaintProperties.pseudonymSecret.toByteArray(Charsets.UTF_8).size
        require(secretSize >= MIN_SECRET_BYTES) {
            "Pseudonym secret must be at least $MIN_SECRET_BYTES bytes (현재: $secretSize)"
        }
    }

    fun generate(studentId: String): String {
        val secret = rusaintProperties.pseudonymSecret
        val keySpec = SecretKeySpec(secret.toByteArray(), HMAC_SHA256)
        val mac = Mac.getInstance(HMAC_SHA256)
        mac.init(keySpec)
        val digest = mac.doFinal(studentId.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
    }

    companion object {
        private const val HMAC_SHA256 = "HmacSHA256"
        private const val MIN_SECRET_BYTES = 32
    }
}
