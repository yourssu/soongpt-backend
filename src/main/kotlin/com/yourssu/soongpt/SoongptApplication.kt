package com.yourssu.soongpt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
class SoongptApplication

fun main(args: Array<String>) {
	runApplication<SoongptApplication>(*args)
}
