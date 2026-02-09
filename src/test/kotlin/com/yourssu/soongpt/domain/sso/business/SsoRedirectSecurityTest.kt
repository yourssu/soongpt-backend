package com.yourssu.soongpt.domain.sso.business

import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.config.SsoProperties
import com.yourssu.soongpt.domain.sso.implement.SyncSessionStore
import com.yourssu.soongpt.domain.usaint.implement.PseudonymGenerator
import com.yourssu.soongpt.domain.usaint.implement.RusaintServiceClient
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import jakarta.servlet.http.Cookie
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Base64

/**
 * SSO 콜백의 redirect URL 보안 검증 테스트.
 *
 * - Open Redirect 방어 (allowlist 외 URL 차단)
 * - 특수문자/프로토콜 공격 방어
 * - Base64 디코딩 실패 시 fallback
 * - 매우 긴 URL 처리
 */
class SsoRedirectSecurityTest : BehaviorSpec({

    val defaultFrontendUrl = "https://soongpt.yourssu.com"
    val allowedUrl = "http://localhost:5173"

    // 공통 mock 설정
    fun createSsoService(
        allowedRedirectUrls: List<String> = listOf(allowedUrl),
    ): SsoService {
        val ssoProperties = SsoProperties(
            frontendUrl = defaultFrontendUrl,
            clientJwtSecret = "test-secret-at-least-48-bytes-for-validation-ok!!!!!!!!",
            allowedRedirectUrls = allowedRedirectUrls,
            sessionTtlMinutes = 60L,
            jwtValidityMinutes = 60L,
        )

        val mockRusaintClient = mock<RusaintServiceClient> {
            doNothing().`when`(it).validateToken(any(), any())
        }

        val mockPseudonymGenerator = mock<PseudonymGenerator> {
            on { generate(any()) } doReturn "test-pseudonym"
        }

        val clientJwtProvider = ClientJwtProvider(ssoProperties)

        val syncSessionStore = SyncSessionStore(ssoProperties)

        return SsoService(
            ssoProperties = ssoProperties,
            pseudonymGenerator = mockPseudonymGenerator,
            clientJwtProvider = clientJwtProvider,
            syncSessionStore = syncSessionStore,
            rusaintServiceClient = mockRusaintClient,
        )
    }

    // 유효한 sToken (200~700자, Base64 문자셋)
    val validSToken = "A".repeat(300)

    given("Open Redirect 방어") {

        `when`("악의적인 외부 URL로 redirect 시도") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "https://evil.com",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }

        `when`("allowlist에 등록된 URL로 redirect 시도") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = allowedUrl,
            )

            then("허용된 URL로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith allowedUrl
            }
        }

        `when`("allowlist가 비어있으면 모든 redirect가 차단되어야 한다") {
            val service = createSsoService(allowedRedirectUrls = emptyList())
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "http://localhost:5173",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }
    }

    given("프로토콜 공격 방어") {

        `when`("javascript: 프로토콜로 redirect 시도") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "javascript:alert(1)",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }

        `when`("ftp: 프로토콜로 redirect 시도") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "ftp://evil.com/malware",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }

        `when`("data: URI로 redirect 시도") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "data:text/html,<script>alert(1)</script>",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }
    }

    given("특수문자가 포함된 redirect URL") {

        `when`("URL에 특수문자가 포함된 경우") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "https://evil.com/<script>alert(1)</script>",
            )

            then("allowlist에 없으므로 기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }

        `when`("빈 문자열로 redirect 시도") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }

        `when`("공백만 있는 redirect URL") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = "   ",
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }
    }

    given("매우 긴 URL 처리") {

        `when`("10,000자 길이의 URL로 redirect 시도") {
            val service = createSsoService()
            val longUrl = "https://evil.com/" + "a".repeat(10_000)
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = longUrl,
            )

            then("allowlist에 없으므로 기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }
    }

    given("null redirect URL") {

        `when`("redirect가 null인 경우") {
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = null,
            )

            then("기본 frontendUrl로 리다이렉트해야 한다") {
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }
    }

    given("Base64 URL-safe 디코딩 (컨트롤러 레벨 검증)") {

        `when`("정상적인 Base64 인코딩된 URL") {
            val original = "http://localhost:5173"
            val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(original.toByteArray())
            val decoded = String(Base64.getUrlDecoder().decode(encoded))

            then("원래 URL로 정상 디코딩되어야 한다") {
                decoded shouldBe original
            }
        }

        `when`("잘못된 Base64 문자열") {
            val invalidBase64 = "!!!not-valid-base64@@@"
            val result = try {
                String(Base64.getUrlDecoder().decode(invalidBase64))
            } catch (e: IllegalArgumentException) {
                null
            }

            then("디코딩 실패하여 null이어야 한다") {
                result shouldBe null
            }
        }

        `when`("악의적인 URL이 Base64로 인코딩된 경우") {
            val evilUrl = "https://evil.com"
            val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(evilUrl.toByteArray())
            val decoded = String(Base64.getUrlDecoder().decode(encoded))

            // 디코딩은 성공하지만, SsoService.resolveRedirectBase에서 allowlist 검증에 의해 차단됨
            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = decoded,
            )

            then("디코딩은 성공하지만 allowlist에 없으므로 기본 frontendUrl로 리다이렉트해야 한다") {
                decoded shouldBe evilUrl
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }

        `when`("매우 긴 Base64 문자열") {
            val longUrl = "https://example.com/" + "a".repeat(5000)
            val encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(longUrl.toByteArray())
            val decoded = String(Base64.getUrlDecoder().decode(encoded))

            val service = createSsoService()
            val result = service.handleCallback(
                sToken = validSToken,
                studentId = "20231234",
                referer = null,
                redirectUrl = decoded,
            )

            then("디코딩은 성공하지만 allowlist에 없으므로 기본 frontendUrl로 리다이렉트해야 한다") {
                decoded shouldBe longUrl
                result.redirectUrl shouldStartWith defaultFrontendUrl
            }
        }
    }
})
