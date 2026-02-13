package com.yourssu.soongpt.common.filter

import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.common.config.ClientJwtProvider
import com.yourssu.soongpt.common.config.SsoProperties
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import jakarta.servlet.FilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse

class CurrentPseudonymFilterTest : BehaviorSpec({

    fun props(): SsoProperties {
        return SsoProperties(
            frontendUrl = "https://soongpt.yourssu.com",
            clientJwtSecret = "x".repeat(32),
            allowedRedirectUrls = emptyList(),
            sessionTtlMinutes = 60L,
            jwtValidityMinutes = 60L,
            cookieSameSite = "Lax",
            cookieSecure = false,
        )
    }

    lateinit var provider: ClientJwtProvider
    lateinit var filter: CurrentPseudonymFilter

    beforeTest {
        CurrentPseudonymHolder.clear()
        provider = ClientJwtProvider(props())
        filter = CurrentPseudonymFilter(provider)
    }

    given("CurrentPseudonymFilter") {
        `when`("유효한 auth cookie가 있으면") {
            then("FilterChain 실행 중 pseudonym이 세팅되고, 완료 후 clear된다") {
                val token = provider.issueToken("test-pseudonym")

                val request = MockHttpServletRequest().apply {
                    setCookies(provider.createAuthCookie(token))
                }
                val response = MockHttpServletResponse()

                var pseudonymInChain: String? = null
                val chain = FilterChain { _, _ ->
                    pseudonymInChain = CurrentPseudonymHolder.get()
                }

                filter.doFilter(request, response, chain)

                pseudonymInChain shouldBe "test-pseudonym"
                CurrentPseudonymHolder.get().shouldBeNull()
            }
        }

        `when`("auth cookie가 없으면") {
            then("FilterChain 실행 중에도 pseudonym은 null이다") {
                val request = MockHttpServletRequest()
                val response = MockHttpServletResponse()

                var pseudonymInChain: String? = "not-null"
                val chain = FilterChain { _, _ ->
                    pseudonymInChain = CurrentPseudonymHolder.get()
                }

                filter.doFilter(request, response, chain)

                pseudonymInChain.shouldBeNull()
                CurrentPseudonymHolder.get().shouldBeNull()
            }
        }

        `when`("auth cookie가 있지만 토큰이 유효하지 않으면") {
            then("FilterChain 실행 중 pseudonym은 null이다") {
                val request = MockHttpServletRequest().apply {
                    setCookies(provider.createAuthCookie("invalid-token"))
                }
                val response = MockHttpServletResponse()

                var pseudonymInChain: String? = "not-null"
                val chain = FilterChain { _, _ ->
                    pseudonymInChain = CurrentPseudonymHolder.get()
                }

                filter.doFilter(request, response, chain)

                pseudonymInChain.shouldBeNull()
                CurrentPseudonymHolder.get().shouldBeNull()
            }
        }
    }
})
