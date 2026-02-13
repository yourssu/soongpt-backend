# PT-160 CROSS_MAJOR 조회 분기 구현 보고서

## 1) 사전 백업(Dump)
- 생성 파일: `script/26-1/dump/soongpt_dev_2026-02-13_pre_pt160_cross_major_tables.sql`
- 생성 시각(KST): 2026-02-13 12:54
- 파일 크기: 약 1.8MB
- 포함 테이블:
  - `department`
  - `course`
  - `target`
  - `course_secondary_major_classification`
- 목적: PT-160 구현 변경 전 기준 스냅샷 확보(복구/비교 가능 상태)

## 2) 구현 변경 요약
`/api/courses/by-track`에서 `trackType=CROSS_MAJOR` 조회 기준을 `target` 기반에서 **분류 테이블 원본 기준**으로 분기.

### 핵심 정책 반영
- `DOUBLE_MAJOR`, `MINOR`
  - 기존 유지: `target` Allow-Deny / scope / grade 필터 적용
- `CROSS_MAJOR`
  - 신규 분기: `course_secondary_major_classification` 기준 조회
  - `target` 필터 미적용
  - `completionType` 미지정 시 `RECOGNIZED` 기본값 사용

### 코드 변경
- `CourseServiceImpl.findAllByTrack`
  - `CROSS_MAJOR` 분기 추가
  - 분기 경로에서 `courseReader.findCoursesBySecondaryMajorClassification(...)` 사용
  - `CROSS_MAJOR` 경로에서 불필요한 `college` 조회 제거
- `CourseRepository`
  - `findCoursesBySecondaryMajorClassification(...)` 신규 메서드 추가
- `CourseRepositoryImpl`
  - Querydsl 구현 추가 (`selectDistinct` + `course_secondary_major_classification` join)
- `CourseReader`
  - 신규 repository 메서드 래퍼 추가
- 문서
  - `CourseController` OpenAPI 설명에 트랙별 조회 기준 명시
  - `src/main/resources/api/course/get_courses_by_track.md` 동기화

## 3) 테스트 및 검증
### 신규 테스트
- `src/test/kotlin/com/yourssu/soongpt/domain/course/business/CourseServiceImplByTrackTest.kt`
  - 케이스 1: `CROSS_MAJOR + RECOGNIZED` → classification 조회 메서드만 사용
  - 케이스 2: `CROSS_MAJOR + completionType 없음` → `RECOGNIZED` 기본 조회
  - 케이스 3: `DOUBLE_MAJOR + REQUIRED` → 기존 target 기반 학년(1~5) 루프 유지

### 실행 결과
- `./gradlew test --tests 'com.yourssu.soongpt.domain.course.business.CourseServiceImplByTrackTest'`
  - 결과: **BUILD SUCCESSFUL**
- `./gradlew test --tests 'com.yourssu.soongpt.domain.course.business.SecondaryMajorCourseRecommendServiceTest'`
  - 결과: **BUILD SUCCESSFUL**

### 이슈 및 조치
- 초기 NPE 원인:
  - Kotest `BehaviorSpec`에서 서비스 호출이 등록 시점에 실행되어 `beforeEach` 스텁 적용 전 `department/college` mock 반환값이 null
- 조치:
  - 서비스 호출을 `then` 블록으로 이동(실행 시점 보정)
  - `beforeTest`에서 `reset + 공통 스텁` 적용

## 4) 변경 파일
- `src/main/kotlin/com/yourssu/soongpt/domain/course/business/CourseServiceImpl.kt`
- `src/main/kotlin/com/yourssu/soongpt/domain/course/implement/CourseRepository.kt`
- `src/main/kotlin/com/yourssu/soongpt/domain/course/implement/CourseReader.kt`
- `src/main/kotlin/com/yourssu/soongpt/domain/course/storage/CourseRepositoryImpl.kt`
- `src/main/kotlin/com/yourssu/soongpt/domain/course/application/CourseController.kt`
- `src/main/resources/api/course/get_courses_by_track.md`
- `src/test/kotlin/com/yourssu/soongpt/domain/course/business/CourseServiceImplByTrackTest.kt`
- `script/26-1/dump/soongpt_dev_2026-02-13_pre_pt160_cross_major_tables.sql`

## 5) 후속 확인(배포 후)
- dev API 전수 검증 권장:
  - 기준값: `course_secondary_major_classification`의 `(department, CROSS_MAJOR, RECOGNIZED)`별 `distinct course_code`
  - 비교값: `/api/courses/by-track?...trackType=CROSS_MAJOR&completionType=RECOGNIZED` 응답 count
  - 목표: 학과별 기준값과 API count 일치
