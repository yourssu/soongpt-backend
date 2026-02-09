package com.yourssu.soongpt.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(
                Components()
                    .addSecuritySchemes(
                        "cookieAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.APIKEY)
                            .`in`(SecurityScheme.In.COOKIE)
                            .name("soongpt_auth")
                            .description("SSO 인증 후 발급된 JWT 쿠키")
                    )
            )
            .servers(listOf(Server().apply { url = "/" }))
            .info(
                Info()
                    .title("Soongpt API Document")
                    .version("v0.0.1")
                    .description("""
                        ## 인증 방법
                        1. **로컬 테스트**: `/api/sso/dev/test-token` (POST) 호출하여 테스트 토큰 발급
                        2. **실제 인증**: `/api/sso/callback` (GET)으로 SSO 로그인 후 쿠키 발급
                        3. Swagger UI에서 "Authorize" 버튼 클릭 후 쿠키 값 입력
                        4. 또는 브라우저 개발자 도구에서 쿠키 확인 후 직접 입력
                    """.trimIndent())
            )
    }
}
