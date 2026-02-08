package com.yourssu.soongpt.common.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod.*
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class, SsoProperties::class, RusaintProperties::class)
class WebConfig {
    @Bean
    fun webMvcConfigurer(corsProperties: CorsProperties): WebMvcConfigurer {
        return object : WebMvcConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                val origins = corsProperties.allowedOrigins.split("&").toTypedArray()
                val useCredentials = !origins.contains("*")

                registry.addMapping("/**")
                    .allowedOrigins(*origins)
                    .allowedHeaders("*")
                    .allowedMethods(GET.name(), POST.name(), PUT.name(), DELETE.name(), OPTIONS.name())
                    .allowCredentials(useCredentials)
            }
        }
    }
}
