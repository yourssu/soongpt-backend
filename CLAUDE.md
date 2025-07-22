# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**soongpt-backend** is a Spring Boot 3.4.1 application written in Kotlin that provides 숭실대학교 시간표 생성 및 강의 검색 시스템. The project follows Clean Architecture principles with Domain-Driven Design patterns.

## Development Commands

### Build and Test
```bash
# Clean build with tests
./gradlew clean build

# Run tests only  
./gradlew test

# Build without tests
./gradlew build -x test
```

### Running the Application
```bash
# Local development (H2 database)
./gradlew bootRun --args='--spring.profiles.active=local'

# Production deployment
script/run.sh
```

### Development Environment
- **H2 Console**: http://localhost:8080/h2-console (local/test profiles)
  - JDBC URL: `jdbc:h2:mem:testdb`
- **Swagger UI**: http://localhost:8080/swagger-ui
- **Default Port**: 8080 (configurable via SERVER_PORT)

## Architecture

### Core Structure
```
src/main/kotlin/com/yourssu/soongpt/
├── common/                    # Shared utilities and configurations
│   ├── business/             # Business logic utilities  
│   ├── config/               # Spring configurations
│   ├── handler/              # Global exception handlers
│   └── infrastructure/       # External service integrations
└── domain/                   # Core business domains
    ├── college/              # 단과대학 management
    ├── course/               # 강의 management and search
    ├── courseTime/           # 강의 시간 scheduling
    ├── department/           # 학과 management
    ├── departmentGrade/      # 학년별 학과 requirements
    ├── rating/               # 강의 평가
    └── timetable/            # 시간표 generation with strategies
```

### Key Architectural Patterns
- **Clean Architecture**: Clear separation between domain, application, and infrastructure layers
- **Strategy Pattern**: Multiple timetable generation strategies (FreeDayTag, NoMorningClasses, etc.)
- **Domain-Driven Design**: Rich domain models with business logic encapsulation

## Technology Stack

- **Language**: Kotlin 1.9.25 on JDK 21
- **Framework**: Spring Boot 3.4.1 with Spring Data JPA
- **Database**: H2 (local/test), MySQL (production)  
- **Query Builder**: QueryDSL for type-safe database queries
- **Testing**: Kotest + JUnit 5 + Mockito-Kotlin
- **Documentation**: SpringDoc OpenAPI
- **HTTP Client**: Spring Cloud OpenFeign

## Environment Profiles

- **local**: H2 database, CORS enabled for all origins
- **test**: H2 database with debug logging for tests
- **dev/prod**: MySQL database, deployed to EC2 with environment-specific configurations

## Data Management

### Course Data Structure
- **Location**: `src/main/resources/data/` organized by semester (2024_1, 2024_2, 2025_1)
- **Format**: JSON files containing comprehensive course catalogs
- **Initialization**: Automated data loading on application startup

### College/Department Configuration  
- **File**: `src/main/resources/data.yml`
- **Purpose**: Defines college and department hierarchies

## Testing

### Test Framework
- **Primary**: Kotest with BehaviorSpec for readable test structure
- **Data Cleanup**: Automated via DataClearExtension
- **Database**: H2 in-memory with test fixtures

### Test Structure
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "ClassName"
```

## API Development

### Documentation
- **HTTP Examples**: `src/main/resources/http/` contains example API calls
- **API Specs**: `src/main/resources/api/` contains detailed documentation
- **Live Docs**: Swagger UI available during development

### Core Features
1. **Course Search**: 전공/교양 과목 검색 및 필터링
2. **Timetable Generation**: 자동 시간표 생성 with conflict resolution
3. **Rating System**: 강의 평가 및 조회

## CI/CD

### GitHub Actions
- **dev.yml**: Automated build and EC2 deployment for main branch
- **claude.yml**: Korean code review automation using Claude

### Deployment
- **Target**: EC2 instances with environment-specific configurations
- **Process**: Gradle build → JAR packaging → SCP transfer → Service restart