package com.yourssu.soongpt.common.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .components(Components())
            .servers(listOf(Server().apply { url = "/" }))
            .info(
                Info()
                    .title("Soongpt API Document")
                    .version("v0.0.1")
            )
    }
}