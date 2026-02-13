package com.yourssu.soongpt.common.config

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.springframework.mock.web.MockHttpServletRequest

class ClientJwtProviderTest : BehaviorSpec({

    fun props(
        clientJwtSecret: String = "x".repeat(32),
        jwtValidityMinutes: Long = 60L,
        cookieSameSite: String = "Lax",
        cookieSecure: Boolean = false,
    ): SsoProperties {
        return SsoProperties(
            frontendUrl = "https://soongpt.yourssu.com",
            clientJwtSecret = clientJwtSecret,
            allowedRedirectUrls = emptyList(),
            sessionTtlMinutes = 60L,
            jwtValidityMinutes = jwtValidityMinutes,
            cookieSameSite = cookieSameSite,
            cookieSecure = cookieSecure,
        )
    }

    given("ClientJwtProvider") {
        `when`("secret이 32바이트 미만이면") {
            then("초기화 시 예외가 발생한다") {
                shouldThrow<IllegalArgumentException> {
                    ClientJwtProvider(props(clientJwtSecret = "short-secret"))
                }
            }
        }

        `when`("토큰을 발급하고 검증하면") {
            then("동일한 pseudonym을 반환한다") {
                val provider = ClientJwtProvider(props())
                val token = provider.issueToken("test-pseudonym")

                val result = provider.validateAndGetPseudonym(token)

                result.isSuccess shouldBe true
                result.getOrThrow() shouldBe "test-pseudonym"
            }
        }

        `when`("토큰이 위변조되면") {
            then("InvalidTokenException으로 실패한다") {
                val provider = ClientJwtProvider(props())
                val token = provider.issueToken("test-pseudonym")
                val parts = token.split(".")
                val tamperedPayload = parts[1].reversed()
                val tampered = "${parts[0]}.$tamperedPayload.${parts[2]}"

                val result = provider.validateAndGetPseudonym(tampered)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<InvalidTokenException>()
            }
        }

        `when`("만료된 토큰이면") {
            then("TokenExpiredException으로 실패한다") {
                val provider = ClientJwtProvider(props(jwtValidityMinutes = -1L))
                val token = provider.issueToken("test-pseudonym")

                val result = provider.validateAndGetPseudonym(token)

                result.isFailure shouldBe true
                result.exceptionOrNull().shouldBeInstanceOf<TokenExpiredException>()
            }
        }

        `when`("요청에 auth cookie가 없으면") {
            then("extractTokenFromRequest는 null을 반환한다") {
                val provider = ClientJwtProvider(props())
                val request = MockHttpServletRequest()

                provider.extractTokenFromRequest(request).shouldBeNull()
            }
        }

        `when`("요청에 auth cookie가 있으면") {
            then("extractPseudonymFromRequest로 pseudonym을 얻을 수 있다") {
                val provider = ClientJwtProvider(props(cookieSecure = true, cookieSameSite = "Strict"))
                val token = provider.issueToken("test-pseudonym")

                val request = MockHttpServletRequest().apply {
                    setCookies(provider.createAuthCookie(token))
                }

                val result = provider.extractPseudonymFromRequest(request)

                result.isSuccess shouldBe true
                result.getOrThrow() shouldBe "test-pseudonym"
            }
        }

        `when`("로그아웃 쿠키를 생성하면") {
            then("maxAge=0, value는 빈 문자열이다") {
                val provider = ClientJwtProvider(props(cookieSecure = true))
                val cookie = provider.createLogoutCookie()

                cookie.name shouldBe ClientJwtProvider.COOKIE_NAME
                cookie.value shouldBe ""
                cookie.maxAge shouldBe 0
                cookie.isHttpOnly shouldBe true
                cookie.secure shouldBe true
            }
        }
    }
})
