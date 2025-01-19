plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.25"
}

group = "com.yourssu"
version = "0.0.1-SNAPSHOT"

val kotlinVersion = "1.9.25"
val springBootVersion = "3.4.1"
val kotestVersion = "5.9.1"
val jacksonVersion = "2.18.2"
val h2Version = "2.3.232"
val mysqlVersion = "9.1.0"
val junitPlatformVersion = "1.11.4"
val guavaVersion = "33.3.0-jre"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jdbc:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-starter-jdbc:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-starter-validation:$springBootVersion")
	implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")

	runtimeOnly("com.h2database:h2:$h2Version")
	runtimeOnly("com.mysql:mysql-connector-j:$mysqlVersion")

	implementation("com.google.guava:guava:$guavaVersion")

	testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
	testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
	testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:$kotlinVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitPlatformVersion")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

allOpen {
	annotation("jakarta.persistence.Entity")
	annotation("jakarta.persistence.MappedSuperclass")
	annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
	useJUnitPlatform()
}