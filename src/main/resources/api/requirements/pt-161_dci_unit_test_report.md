# PT-161 백엔드 DCI 패턴 단위 테스트 보고서

## 1. 목적
`CourseRecommendApplicationService`의 카테고리별 dispatch(DCI 관점의 Context → Interaction 라우팅)가
의도대로 동작하는지 **격리 단위 테스트(mock 기반)** 로 검증한다.

- 통합 테스트 의존 없이 서비스 단위에서 라우팅/분기/기본 응답(noData)을 확인
- 기존 테스트 스타일(BehaviorSpec, given-when-then)과 형식 일치
- 추천 도메인의 핵심 분기 커버리지 강화

## 2. 작업 범위

### 신규 테스트 파일
- `src/test/kotlin/com/yourssu/soongpt/domain/course/application/CourseRecommendApplicationServiceTest.kt`

### 테스트 설계 원칙
- 외부 의존성 모두 mock 처리
  - `RecommendContextResolver`
  - `MajorCourseRecommendService`
  - `GeneralCourseRecommendService`
  - `RetakeCourseRecommendService`
  - `SecondaryMajorCourseRecommendService`
  - `TeachingCourseRecommendService`
- `HttpServletRequest`도 mock으로 대체
- DB/네트워크 접근 없음

## 3. 케이스 구성 (커버리지 관점)

### A. MAJOR_BASIC 분기
- graduationSummary.majorFoundation 존재 시
  - `majorCourseRecommendService.recommendMajorBasicOrRequired(...)` 호출 검증
  - `Progress` 매핑 전달값 검증
  - warnings가 최종 응답에 보존되는지 검증

### B. MAJOR_REQUIRED noData 분기
- graduationSummary가 null일 때
  - noData 응답(`required=0, completed=0, satisfied=true`) 검증
  - 메시지(`졸업사정표에 전공필수 항목이 없습니다.`) 검증
  - major 서비스 미호출 검증

### C. GENERAL_REQUIRED 분기
- `generalCourseRecommendService.recommend(...)` 라우팅 검증
- `schoolId`, `admissionYear`, `takenSubjectCodes` 전달값 검증

### D. RETAKE 분기
- `retakeCourseRecommendService.recommend(lowGradeSubjectCodes)` 라우팅 검증

### E. SECONDARY / TEACHING 분기
- `DOUBLE_MAJOR_REQUIRED` → `recommendDoubleMajorRequired(ctx)`
- `DOUBLE_MAJOR_ELECTIVE` → `recommendDoubleMajorElective(ctx)`
- `MINOR` → `recommendMinor(ctx)`
- `TEACHING` → `teachingCourseRecommendService.recommend(ctx)`

### F. 멀티 카테고리 순서 보장
- 요청 순서(`RETAKE,MAJOR_BASIC,TEACHING`)대로 응답 카테고리 순서가 유지되는지 검증

## 4. 기존 테스트와의 정합

기존 추천 도메인 단위 테스트 스타일에 맞춰 다음을 준수했다.

- `BehaviorSpec` 사용
- `given / when / then` 한국어 시나리오 명명
- mock 기반 격리 테스트
- 서비스 호출 verify + 응답 값 assert 병행

또한 기존 추천 단위 테스트와 함께 실행해 회귀 여부를 확인했다.

## 5. 실행 및 결과

### 신규 테스트 단독 실행
```bash
./gradlew test --tests 'com.yourssu.soongpt.domain.course.application.CourseRecommendApplicationServiceTest'
```
- 결과: **BUILD SUCCESSFUL**

### 기존 관련 테스트 포함 실행
```bash
./gradlew test \
  --tests 'com.yourssu.soongpt.domain.course.application.CourseRecommendApplicationServiceTest' \
  --tests 'com.yourssu.soongpt.domain.course.business.SecondaryMajorCourseRecommendServiceTest' \
  --tests 'com.yourssu.soongpt.domain.course.business.GeneralCourseRecommendServiceTest'
```
- 결과: **BUILD SUCCESSFUL**

## 6. 결론
- DCI 라우팅의 핵심 분기(major/general/retake/secondary/teaching)와 noData/순서 보장을 단위 수준에서 검증 완료.
- 기존 테스트 형식과 일치하며, 통합 테스트 의존 없이 실제 동작 경로를 격리 검증할 수 있는 기반을 확보했다.
