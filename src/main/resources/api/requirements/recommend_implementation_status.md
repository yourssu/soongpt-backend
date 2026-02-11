# 과목 추천 구현 현황 및 비즈니스 로직 정리

## 1. 전체 아키텍처

```
프론트 → GET /api/courses/recommend/all?category=MAJOR_BASIC,RETAKE,...
          ↓
    CourseController
          ↓
    CourseRecommendApplicationService.recommend()
          ↓
    RecommendContextResolver.resolve()  ← SyncSessionStore에서 rusaint 데이터 꺼냄
          ↓
    dispatch(category, ctx)  ← 이수구분별 라우팅
          ↓
    ┌─ MajorCourseRecommendService      (전기/전필/전선)
    ├─ GeneralCourseRecommendService     (교필/교선)
    ├─ RetakeCourseRecommendService      (재수강)
    └─ (미구현: 복수전공/부전공/교직이수)
```

## 2. 라우팅: dispatch() 흐름

`CourseRecommendApplicationService.dispatch()` 에서 `RecommendCategory` enum으로 분기:

| RecommendCategory    | 호출 서비스                                                        | 현재 상태                               |
| -------------------- | ------------------------------------------------------------------ | --------------------------------------- |
| `MAJOR_BASIC`      | `majorCourseRecommendService.recommendMajorBasicOrRequired()`    | **구현 완료**                     |
| `MAJOR_REQUIRED`   | `majorCourseRecommendService.recommendMajorBasicOrRequired()`    | **구현 완료**                     |
| `MAJOR_ELECTIVE`   | `majorCourseRecommendService.recommendMajorElectiveWithGroups()` | **구현 완료**                     |
| `GENERAL_REQUIRED` | `generalCourseRecommendService.recommend()`                      | **구현 완료**                     |
| `GENERAL_ELECTIVE` | `generalCourseRecommendService.recommend()`                      | **코드 있음, dispatch에서 throw** |
| `RETAKE`           | `retakeCourseRecommendService.recommend()`                       | **구현 완료**                     |
| `DOUBLE_MAJOR_REQUIRED` | 없음 (progress만 제공)                                        | **progress만 반환**               |
| `DOUBLE_MAJOR_ELECTIVE` | 없음 (progress만 제공)                                        | **progress만 반환**               |
| `MINOR`            | 없음                                                               | **미구현 (throw)**                |
| `TEACHING`         | 없음                                                               | **미구현 (throw)**                |

## 3. 공통 데이터 흐름

### 3-1. RecommendContext (rusaint에서 가져온 사용자 데이터)

```
RecommendContext
├── departmentName: String      ← 사용자 학과명
├── userGrade: Int              ← 현재 학년
├── schoolId: Int               ← 입학년도 2자리 (ex: 23)
├── takenSubjectCodes: List<String>  ← 이수 과목 코드 목록 (전 학기 합산)
├── lowGradeSubjectCodes: List<String>  ← C+ 이하 과목 코드 (재수강용)
├── graduationSummary            ← 졸업사정표 요약
│   ├── majorFoundation: { required, completed, satisfied }  ← 전기
│   ├── majorRequired: { required, completed, satisfied }    ← 전필
│   ├── majorElective: { required, completed, satisfied }    ← 전선
│   ├── generalRequired: { required, completed, satisfied }  ← 교필
│   ├── generalElective: { required, completed, satisfied }  ← 교선
│   ├── doubleMajorRequired: { required, completed, satisfied }
│   ├── doubleMajorElective: { required, completed, satisfied }
│   ├── christianCourses: { required, completed, satisfied }
│   └── chapel: { satisfied }
└── flags
    ├── doubleMajorDepartment: String?  ← 복수전공 학과 (null이면 없음)
    ├── minorDepartment: String?        ← 부전공 학과 (null이면 없음)
    └── teaching: Boolean               ← 교직이수 여부
```

### 3-2. 공통 비즈니스 로직 (각 서비스 내부에서 반복)

```
1. progress.satisfied 체크 → true면 "이미 이수" 메시지 반환 (과목 X)
2. DB에서 해당 카테고리 과목 조회 (courseRepository.findCoursesWithTargetByCategory)
3. takenSubjectCodes로 이수 과목 제외 (baseCode 기준)
   - 전공: 과목 단위 제외
   - 교양: 분야 단위 제외 (분야 내 하나라도 이수 → 분야 전체 제외)
4. 남은 과목 없으면 "개설 과목 없음" 메시지 반환
5. 있으면 과목 목록 반환
```

**공통 인터페이스/추상 클래스 없음** — 각 서비스가 독립적으로 위 패턴을 구현 중.

### 3-3. 현재 응답 구조

```json
{
  "warnings": [],
  "categories": [
    {
      "category": "MAJOR_BASIC",
      "progress": { "required": 18, "completed": 12, "satisfied": false },
      "message": null,
      "userGrade": null,
      "courses": [...],
      "gradeGroups": null,
      "fieldGroups": null,
      "lateFields": null
    }
  ]
}
```

- `warnings` — rusaint 동기화 경고 메시지 (top-level)
- `progress == null` — 졸업사정표 없어서 판단 불가 (재수강은 원래 null)
- `message == null` → 정상 (과목 있음)
- `message != null` → 에지케이스 (이미 이수 / 개설 없음)

엣지케이스 상세: `recommend_edge_cases.md` 참고

## 4. 이수구분별 상세 구현 현황

---

### 재수강 (RETAKE) — 구현 완료

**서비스**: `RetakeCourseRecommendService`

**입력**: `lowGradeSubjectCodes` (C+ 이하 과목 코드)

**progress**: 없음 (null)

**비즈니스 로직**:

```
1. lowGradeSubjectCodes 비어있음 → "재수강 가능한 C+ 이하 과목이 없습니다."
2. 해당 baseCodes로 이번 학기 개설 과목 조회 (findCoursesWithTargetByBaseCodes)
3. 개설 과목 없음 → "C+ 이하 과목은 있으나, 이번 학기에 개설되는 재수강 과목이 없습니다."
4. 있으면 과목 목록 반환
```

**이수 과목 필터**: 사용하지 않음 (재수강은 이미 들은 과목을 다시 듣는 것)

**프론트 판단 포인트**:

- C 이하 과목 없는 경우 → SKIP (또는 SHOW_MESSAGE)
- C 이하 있지만 미개설 → SHOW_MESSAGE

---

### 전공기초 (MAJOR_BASIC) — 구현 완료

**서비스**: `MajorCourseRecommendService.recommendMajorBasicOrRequired()`

**입력**: departmentName, userGrade, takenSubjectCodes, progress (from graduationSummary.majorFoundation)

**progress**: `{ required, completed, satisfied }` from 졸업사정표

**비즈니스 로직**:

```
1. progress.satisfied → "전공기초 학점을 이미 모두 이수하셨습니다."
2. findCoursesWithTargetByCategory(MAJOR_BASIC, maxGrade=userGrade)
3. takenSubjectCodes baseCode 비교로 이수 과목 제외
4. 빈 목록 → "이번 학기에 수강 가능한 전공기초 과목이 없습니다."
5. 과목 목록 반환 (LATE/ON_TIME timing 포함)
```

**이수 과목 필터**: baseCode 단위 (과목 단위)

**조회 범위**: 1학년 ~ 현재 학년

**프론트 판단 포인트**:

- 졸업사정표 자체 없음 → WARN
- 졸업사정표에 전기 항목 없음 & 1학년 → SKIP
- 졸업사정표에 전기 항목 없음 & 2학년+ → SHOW_MESSAGE ("미이수이나 수강 가능 과목 없음")

---

### 전공필수 (MAJOR_REQUIRED) — 구현 완료

**서비스**: `MajorCourseRecommendService.recommendMajorBasicOrRequired()`

**입력**: departmentName, userGrade, takenSubjectCodes, progress (from graduationSummary.majorRequired)

**전기와 동일한 로직**, category만 `MAJOR_REQUIRED`로 변경.

**프론트 판단 포인트**:

- 졸업사정표에 전필 항목 없음 → SKIP
- 졸업사정표 satisfied=true → SHOW_MESSAGE ("이미 이수")
- 졸업사정표 satisfied=false & 개설 없음 → SHOW_MESSAGE ("수강 가능 과목 없음")

---

### 전공선택 (MAJOR_ELECTIVE) — 구현 완료

**서비스**: `MajorCourseRecommendService.recommendMajorElectiveWithGroups()`

**입력**: departmentName, userGrade, takenSubjectCodes, progress (from graduationSummary.majorElective)

**비즈니스 로직**:

```
1. progress.satisfied → "전공선택 학점을 이미 모두 이수하셨습니다."
2. findCoursesWithTargetByCategory(MAJOR_ELECTIVE, maxGrade=5)  ← 전체 학년
3. takenSubjectCodes baseCode 비교로 이수 과목 제외
4. 빈 목록 → "이번 학기에 수강 가능한 전공선택 과목이 없습니다."
5. 과목 목록 + gradeGroups(학년별 그룹) 반환
```

**전기/전필과 다른 점**:

- 조회 범위: 전체 학년 (1~5)
- `userGrade` 응답에 포함
- `gradeGroups`: 대상학년별 과목 그룹핑 추가

**프론트 판단 포인트**:

- 다 들었을 경우 → SHOW_MESSAGE (화면은 패스 X, 메시지만)

---

### 교양필수 (GENERAL_REQUIRED) — 구현 완료

**서비스**: `GeneralCourseRecommendService.recommend()`

**입력**: departmentName, userGrade, schoolId, takenSubjectCodes, progress (from graduationSummary.generalRequired)

**비즈니스 로직**:

```
1. progress.satisfied → "교양필수 학점을 이미 모두 이수하셨습니다."
2. findCoursesWithTargetByCategory(GENERAL_REQUIRED, maxGrade=userGrade)
3. course.field를 FieldFinder.findFieldBySchoolId()로 파싱 → 분야별 그룹핑
4. 분야 단위 이수 필터: 분야 내 하나라도 이수 → 분야 전체 제외
5. 미수강 분야 없음 → "이번 학기에 수강 가능한 교양필수 과목이 없습니다."
6. LATE 분야 → lateFields (텍스트만), ON_TIME 분야 → fieldGroups (과목 포함)
```

**전공과 다른 점**:

- 이수 필터: 분야 단위 (과목 하나 이수 → 분야 전체 제거)
- 응답에 `fieldGroups` + `lateFields` 사용 (`courses`는 빈 배열)
- `schoolId`로 입학년도별 분야명 매칭

**프론트 판단 포인트**:

- 졸업사정표 satisfied=true → SHOW_MESSAGE
- 졸업사정표 satisfied=false & 수강 대상 맞는 과목 없음 → SHOW_MESSAGE

---

### 교양선택 (GENERAL_ELECTIVE) — 코드 있으나 비활성화

**서비스**: `GeneralCourseRecommendService.recommend()` (교필과 같은 메서드)

**현재 상태**: dispatch()에서 `throw IllegalArgumentException("교양선택은 별도 API로 제공 예정입니다.")`

**코드상으로는** `buildGeneralElectiveResponse()`가 이미 존재:

- 교필과 동일한 분야별 그룹핑
- LATE/ON_TIME 구분 없이 모든 분야를 fieldGroups로 반환

**dispatch 연결만 하면 동작함** — throw 제거 후 교필과 동일하게 호출하면 됨.

**프론트 판단 포인트**:

- 패스하는 경우 없음 (항상 표시)

---

### 복수전공필수 (DOUBLE_MAJOR_REQUIRED) — progress만 반환

**서비스**: 없음 (과목 추천 미구현, progress만 제공)

**현재 상태**: dispatch()에서 `graduationSummary.doubleMajorRequired`의 progress만 반환. 과목 목록은 빈 배열.

**사용 가능한 데이터**:

- `flags.doubleMajorDepartment`: 복수전공 학과명 (null이면 복수전공 없음)
- `graduationSummary.doubleMajorRequired`: 복수전공 필수 이수 현황
- `courseRepository.findCoursesWithTargetBySecondaryMajor()`: Repository 메서드 존재

**프론트 판단 포인트**:

- `flags.doubleMajorDepartment == null` → 스킵
- `progress.required == 0 && satisfied == true` → 해당 없음 → 스킵
- `progress.satisfied == true && required > 0` → 이미 이수 완료

---

### 복수전공선택 (DOUBLE_MAJOR_ELECTIVE) — progress만 반환

**서비스**: 없음 (과목 추천 미구현, progress만 제공)

**현재 상태**: dispatch()에서 `graduationSummary.doubleMajorElective`의 progress만 반환. 과목 목록은 빈 배열.

**사용 가능한 데이터**:

- `flags.doubleMajorDepartment`: 복수전공 학과명 (null이면 복수전공 없음)
- `graduationSummary.doubleMajorElective`: 복수전공 선택 이수 현황
- `courseRepository.findCoursesWithTargetBySecondaryMajor()`: Repository 메서드 존재

**프론트 판단 포인트**:

- `flags.doubleMajorDepartment == null` → 스킵
- `progress.required == 0 && satisfied == true` → 해당 없음 → 스킵
- `progress.satisfied == true && required > 0` → 이미 이수 완료

---

### 부전공 (MINOR) — 미구현

**서비스**: 없음

**현재 상태**: dispatch()에서 throw

**사용 가능한 데이터**:

- `flags.minorDepartment`: 부전공 학과명 (null이면 부전공 없음)
- graduationSummary에 부전공 전용 항목 없음 (doubleMajor만 있음)
- `courseRepository.findCoursesWithTargetBySecondaryMajor()`: Repository 메서드 존재

**프론트 판단 포인트**:

- `flags.minorDepartment == null` → SKIP (화면 자체 패스)
- 복수전공과 동일한 패턴

---

### 교직이수 (TEACHING) — 미구현

**서비스**: 없음

**현재 상태**: dispatch()에서 throw

**사용 가능한 데이터**:

- `flags.teaching`: 교직이수 여부 (boolean)
- graduationSummary에 교직 전용 항목 없음

**프론트 판단 포인트**:

- `flags.teaching == false` → SKIP (화면 자체 패스)
- `flags.teaching == true` → 이수 정보 못 불러오므로 화면 & 과목 무조건 보여주기

---

## 5. 엣지케이스 처리 방식

서버는 **status/action을 결정하지 않음** — 프론트가 아래 조합으로 판단.

자세한 엣지케이스 → 응답 매핑은 `recommend_edge_cases.md` 참고.

### 프론트 판단 로직 요약

```
1. warnings에 값 있음 → 경고 배너 표시
2. progress == null && category != RETAKE → 졸업사정표 없어서 판단 불가
3. progress.required == 0 && satisfied == true → 해당 없는 이수구분 → 스킵
4. progress.satisfied == true && required > 0 → 이미 이수 완료
5. courses/fieldGroups 비어있음 → 이번 학기 개설 없음
6. courses/fieldGroups 있음 → 정상 렌더링
```
