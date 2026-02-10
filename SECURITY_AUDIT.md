# soongpt-backend 보안 취약점 점검 보고서

- 점검 일시: 2026-02-10
- 대상 브랜치: `feature/pt-133`
- 대상 경로: `soongpt-backend` 전체
- 점검 방식: 수동 코드리뷰 + grep 기반 정적 점검
- 참고: semgrep 미설치, Gradle 의존성 전체 자동 스캔은 환경/toolchain 이슈로 미완료

---

## 1) [Critical] 인증·인가 계층 부재 (Broken Access Control)

### 근거
- `build.gradle.kts:38-78`
  - `spring-boot-starter-security` 의존성 부재
- `src/main/kotlin/.../domain/timetable/application/TimetableController.kt:12-29`
  - `/api/timetables/{id}` 조회 엔드포인트 인증/인가 체크 부재
- `src/main/kotlin` 하위 security/auth/jwt/session 관련 구성 파일 부재

### 공격 시나리오
- ID 순차 대입(`GET /api/timetables/1..N`)으로 타 사용자 데이터 열람 가능(IDOR)
- 쓰기 API(`POST /api/contacts`, `POST /api/timetables`) 무인증 호출로 스팸/오남용 가능

### 권고사항
- Spring Security 도입 및 기본 deny-all
- API별 인증(JWT/OAuth2) + 권한 검증(메서드/리소스 레벨)
- 시간표 리소스에 owner 개념 추가 후 `id + owner` 조건 강제

---

## 2) [High] 과도한 요청/응답/헤더 로깅으로 민감정보 유출 위험

### 근거
- `src/main/kotlin/.../common/filter/LoggingFilter.kt:31-39`
  - 모든 헤더/요청 body/응답 body를 INFO로 기록
- `src/main/resources/logback-spring.xml:40-56`
  - `prod` 프로파일에서도 파일+콘솔 로그 출력

### 공격 시나리오
- 로그 접근 가능자에 의한 토큰/개인정보 재사용
- 대용량 body 전송으로 로그 폭증, 성능 저하/비용 증가

### 권고사항
- `Authorization`, `Cookie`, `Set-Cookie` 마스킹/제외
- 운영 환경에서 body 전체 로깅 금지(샘플링/길이 제한)
- PII redaction 정책 적용

---

## 3) [High] 시간표 생성 API 입력 검증 누락

### 근거
- `src/main/kotlin/.../domain/timetable/application/TimetableController.kt:18`
  - `createTimetable(@RequestBody request: TimetableCreatedRequest)`에 `@Valid` 누락
- `src/main/kotlin/.../domain/timetable/application/dto/TimetableCreatedRequest.kt`
  - 제약 어노테이션 존재하나 컨트롤러 검증 트리거 미적용

### 공격 시나리오
- 비정상 대량 입력(`codes`, `majorRequiredCodes`)으로 DB/연산 과부하 유발

### 권고사항
- `@Valid` 적용
- 리스트 필드 `@Size(max=...)` 및 원소 범위/중복 제한
- 요청 크기 제한 + rate limiting 병행

---

## 4) [Medium] 내부 예외 메시지 직접 노출 (정보노출)

### 근거
- `src/main/kotlin/.../common/handler/InternalServerErrorControllerAdvice.kt:30`
- `src/main/kotlin/.../common/handler/GlobalControllerAdvice.kt:117,131,145,159`
  - `e.message`를 외부 응답에 포함

### 공격 시나리오
- 내부 구조/쿼리 단서 수집으로 공격 정밀도 상승

### 권고사항
- 외부 응답은 일반화된 메시지+오류코드만 반환
- 상세 원인은 내부 보안 로그로 분리

---

## 5) [Medium] CORS 와일드카드 설정 및 환경 혼입 위험

### 근거
- `src/main/resources/application-local.yml:23` → `allowed-origins: "*"`
- `src/main/kotlin/.../common/config/WebConfig.kt:17-21` 전역 `/**` 매핑

### 공격 시나리오
- 로컬 설정이 운영 혼입 시 임의 Origin 허용 가능

### 권고사항
- 운영은 화이트리스트 Origin만 허용
- CI/CD에서 `*` 금지 정책 검사

---

## 점검 요약

- 하드코딩 비밀정보: 명시적 하드코딩 키/토큰 미발견
- SQLi/unsafe deserialization/SSRF/RCE: 직접적 고위험 패턴 미발견
- JWT/세션 취약점: 메커니즘 자체 부재(인증체계 부재가 핵심 리스크)
- 의존성 취약점: 자동 스캔 미완료, `kotlinx-serialization-json:1.2.0` 업데이트 권장

---

## 우선순위 조치 체크리스트

1. **즉시**: 인증/인가 계층 도입 (IDOR 차단 포함)
2. **즉시**: 로깅 민감정보 마스킹/본문 로깅 제거
3. **즉시**: 입력 검증 강화 (`@Valid`, 크기/범위 제한)
4. **단기**: 예외 응답 하드닝 (`e.message` 외부 노출 제거)
5. **단기**: CORS/프로파일 가드레일 구축 (`*` 금지)
6. **단기**: 의존성 보안 스캔 CI 통합 (OWASP Dependency-Check/Snyk/OSV)
