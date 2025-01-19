package com.yourssu.soongpt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SoongptApplication

fun main(args: Array<String>) {
	runApplication<SoongptApplication>(*args)
}
