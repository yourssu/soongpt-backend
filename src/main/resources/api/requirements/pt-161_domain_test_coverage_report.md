# PT-161 도메인 기준 단위 테스트 커버리지 보고서

## 1) 목적
- 백엔드 단위 테스트를 **도메인 기준으로 커버리지(라인 커버리지)** 를 확인하고,
- 추천(DCI Context/Interaction) 관련 도메인을 중심으로 **격리 단위 테스트를 보강**하여
  실제 동작(분기/정렬/필터링/호출 관계)을 검증한다.

> 주의: 이 리포트의 수치는 `./gradlew test` 기본 실행 기준입니다.
> (build.gradle.kts 설정에 의해 Integration 테스트는 기본 제외)

## 2) 커버리지 산출 방법
### 실행
```bash
./gradlew test
```

### 리포트 위치
- HTML: `build/reports/jacoco/test/html/index.html`
- XML: `build/reports/jacoco/test/jacocoTestReport.xml`

## 3) 도메인(모듈)별 라인 커버리지 요약
(covered/total lines)

- `college`: **74.29%** (26/35)
- `department`: **70.45%** (31/44)
- `usaint`: **55.00%** (88/160)
- `courseTime`: **51.61%** (32/62)
- `course`: **35.41%** (705/1991)
- `common`: **44.22%** (172/389)
- `coursefield`: **30.56%** (11/36)
- `equivalence`: **28.12%** (9/32)
- `contact`: **25.00%** (9/36)
- `rating`: **22.73%** (10/44)
- `sso`: **44.92%** (177/394)
- `admin`: **12.90%** (4/31)
- `timetable`: **9.24%** (91/985)
- `target`: **12.77%** (24/188)

## 4) course 도메인 하위 패키지별 관찰
추천 로직(DCI)과 직결되는 `course.application` / `course.business` / `course.implement`의 커버리지는 상승했지만,
Querydsl 기반 조회(`course.storage`)는 격리 단위 테스트로 커버하기 어려워 상대적으로 낮습니다.

- `course.business`: **49.22%** (412/837)
- `course.application`: **47.02%** (71/151)
- `course.implement`: **35.26%** (67/190)
- `course.business.dto`: **30.45%** (67/220)
- `course.application.dto`: **18.42%** (28/152)
- `course.storage`: **2.41%** (7/291)

## 5) PT-161에서 반영한 테스트 보강/정리
### 신규/보강한 격리 단위 테스트
- `CourseRecommendApplicationServiceTest`
  - 카테고리 dispatch(라우팅) + noData + 순서 보장 검증
- `MajorCourseRecommendServiceTest`
  - 전기/전필: satisfied shortcut, takenSubjectCodes(baseCode) 필터, LATE 우선 정렬
  - 전선(통합용): 전선 + 타전공인정 결합 시 전선 우선 + isCrossMajor 플래그 검증
- `TeachingCourseRecommendServiceTest`
  - 교직이수 대상 여부
  - targetReader(일반/교직용) 학년 루프 호출
  - field 기반 영역(전공/교직/특성화) 분류 및 정렬, 분반(baseCode) 그룹핑
- `TargetReaderTest`
  - 전기/전필(1~userGrade), 전선(1~5) 과목코드 조회 범위 + distinct 검증
- `ClientJwtProviderTest`
  - JWT 발급/검증/만료/위변조, auth/logout 쿠키 속성 검증
- `CurrentPseudonymFilterTest`
  - FilterChain 실행 중 pseudonym set, 종료 후 clear 검증
- `SyncSessionStoreTest`
  - 세션 생성/상태 업데이트/데이터 저장 동작 검증
- `SsoControllerTest`
  - `/sync/status`, `/sync/student-info` 주요 분기(쿠키 없음/무효/세션 만료/정상) 검증

### 기존 테스트 형식 정리 (Kotest 기준 통일)
- `GetTeachingCoursesRequestTest` (JUnit → Kotest)
- `GetFieldByCourseCodeTest` (JUnit → Kotest)
- `CourseReaderTeachingFieldTest` (JUnit → Kotest)

> SpringBootTest 기반 테스트(`SoongptApplicationTests`, `RusaintPropertiesTest`)와
> MockMvc 기반 Integration 테스트는 성격이 달라 JUnit 형식을 유지합니다.

## 6) 다음 보강 후보 (선택)
- `course.application.dto` (입력 파싱/예외) 테스트 추가 → 단위로 커버리지 쉽게 상승
- `course.storage`는 H2/real MySQL 기반의 Repository 통합 테스트가 필요 (격리 단위 테스트로는 한계)
