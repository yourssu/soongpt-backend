# 변경 요약

## 1) 컨트롤러: pseudonym 처리
- `TimetableController`에서 아래 엔드포인트에 대해 요청마다 pseudonym을 추출하고 `CurrentPseudonymHolder`에 세팅하도록 변경.
  - `POST /api/timetables` (createTimetable)
  - `POST /api/timetables/finalize` (finalizeTimetable)
  - `GET /api/timetables/{id}/available-general-electives`
  - `GET /api/timetables/{id}/available-chapels`
- 서비스 호출 시 userId 전달 제거.

## 2) createTimetable 응답 분기
- 연속 if문 → `when(response.status)`로 정리.
- 성공 시 알람 전송 로직 제거 (최종 확정 시에만 알람).

## 3) finalizeTimetable 알람
- finalize 시 pseudonym 세팅 후 알람 전송.

## 4) 채플/교선 응답 로직
- 채플: progress.satisfied == true면 courses는 빈 리스트 응답.
- 교선: progress와 field별 courses 반환 유지.

## 5) available-*에서 시간표 미존재 시 404
- `TimetableService.getAvailableGeneralElectives`, `getAvailableChapels` 시작 시 `ensureTimetableExists` 호출하여
  없는 시간표는 `TimetableNotFoundException` → 404.

## 6) UserContext 제거
- `UserContext`, `UserContextProvider`, `UntakenCourseFetcher`, `TakenCourseChecker` 제거.
- 추천/교체 로직에서 userId 기반 흐름 제거.

## 7) 교체/추천 로직에서 미수강 코드 사용
- `SwapCourseProvider`: `UntakenCourseCodeService.getUntakenCourseCodes(category)` 기반으로 대체 과목 조회.
- `CourseCandidateProvider`: 카테고리별 미수강 코드로 후보 생성.
  - 교필/교선: `getUntakenCourseCodesByField` 사용
  - 그 외: `getUntakenCourseCodes` 사용

## 8) mandatory chapel 판단 복구
- `TimetableRecommendationFacade`에서 `RecommendContextResolver.resolve()`로
  `userGrade`, `departmentName` 확보 후 1학년 채플 강제 포함 판단.
- 채플 후보는 `getUntakenCourseCodes(Category.CHAPEL)`에서 1개 가져와 생성.

## 9) 설명(description) 과목명 보강
- `buildRecommendationDescription`에서 과목명이 비는 문제를 막기 위해
  선택 과목들의 이름을 추가 조회하여 캐시에 포함.
- 분반 미선택 과목은 `baseCode * 100 + 1`로 full code 생성 후 이름 조회.

## 10) 컴파일/테스트 체크
- `./gradlew test` 실행 결과: BUILD SUCCESSFUL.
  - 경고 1건: `TimetableRecommendationFacade.kt` “No cast needed” (기능 영향 없음)

---

# 앞으로 해야 할 일 (선택 사항)

1. **채플 강제 포함 로직 정확성 검증**
   - `getUntakenCourseCodes(Category.CHAPEL)`이 1학년 채플 정책과 정확히 일치하는지 확인 필요.

2. **description 캐시 타입 경고 정리**
   - `mapValues { (_, name) -> name as String? }` 캐스트 제거 가능.

3. **카테고리 매핑 정책 점검**
   - `PrimaryTimetableCommand.findCategoryFor()`에서 `retakeCourses`, `addedCourses`는 현재 `MAJOR_ELECTIVE`로 처리됨.
   - 필요 시 더 정확한 매핑 정책 확정.

