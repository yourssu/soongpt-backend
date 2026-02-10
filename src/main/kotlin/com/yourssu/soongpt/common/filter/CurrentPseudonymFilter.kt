package com.yourssu.soongpt.common.filter

import com.yourssu.soongpt.common.auth.CurrentPseudonymHolder
import com.yourssu.soongpt.common.config.ClientJwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * HTTP 요청 진입 시 쿠키에서 pseudonym을 추출해 [CurrentPseudonymHolder]에 세팅한다.
 * 인증이 필요한 API는 컨트롤러에서 pseudonym을 넘기지 않고, 서비스에서 [CurrentPseudonymHolder.get]으로 조회하면 된다.
 */
@Component
@Order(0) // 요청 진입 시 가장 먼저 실행되어 holder 세팅
class CurrentPseudonymFilter(
    private val clientJwtProvider: ClientJwtProvider,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            clientJwtProvider.extractPseudonymFromRequest(request)
                .onSuccess { CurrentPseudonymHolder.set(it) }
            filterChain.doFilter(request, response)
        } finally {
            CurrentPseudonymHolder.clear()
        }
    }
}
