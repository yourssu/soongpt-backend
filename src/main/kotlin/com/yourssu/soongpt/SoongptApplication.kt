package com.yourssu.soongpt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableAsync

@SpringBootApplication
@EnableFeignClients
@EnableAsync
@ConfigurationPropertiesScan
class SoongptApplication

fun main(args: Array<String>) {
	runApplication<SoongptApplication>(*args)
}
