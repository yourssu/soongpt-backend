package com.yourssu.soongpt

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.io.File

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableJpaAuditing
class SoongptApplication

fun main(args: Array<String>) {
	// logback FILE 앱ender가 쓰기 전에 logs/ 생성 (observer.py 감시 경로). 없으면 파일 쓰기 실패로 로그 파일이 안 생김
	File("logs").mkdirs()
	runApplication<SoongptApplication>(*args)
}
