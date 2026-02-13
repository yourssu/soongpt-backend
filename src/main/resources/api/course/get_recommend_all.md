# 통합 과목 추천 API

## 개요

SSO 인증된 사용자의 학적정보(rusaint)를 기반으로, 요청한 이수구분의 추천 과목을 반환합니다.

## Endpoint

```
GET /api/courses/recommend/all?category={categories}
```

## 인증

- `soongpt_auth` 쿠키 (JWT) 필수

## Request

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| `category` | String | O | 콤마 구분 이수구분. 아래 enum 값 사용 |

### RecommendCategory

| 값 | 설명 |
|---|---|
| `MAJOR_BASIC` | 전공기초 |
| `MAJOR_REQUIRED` | 전공필수 |
| `MAJOR_ELECTIVE` | 전공선택 |
| `GENERAL_REQUIRED` | 교양필수 |
| `RETAKE` | 재수강 |
| `DOUBLE_MAJOR_REQUIRED` | 복수전공필수 |
| `DOUBLE_MAJOR_ELECTIVE` | 복수전공선택 |
| `MINOR` | 부전공 |
| `TEACHING` | 교직이수 |

> 교양선택은 이 API에서 지원하지 않으며, `category`에 포함하면 400 Bad Request를 반환합니다.

### 요청 예시

```
GET /api/courses/recommend/all?category=MAJOR_REQUIRED,MAJOR_ELECTIVE,GENERAL_REQUIRED
```

---

## Response

```
Response<CourseRecommendationsResponse>
```

### 최상위 구조

```json
{
  "timestamp": "2025-03-01 12:00:00",
  "result": {
    "warnings": [],
    "categories": [ CategoryRecommendResponse, ... ]
  }
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `warnings` | String[] | 경고 코드 목록. 빈 배열이면 경고 없음. 코드별 의미는 아래 [warnings 코드](#warnings-코드) 참고 |
| `categories` | CategoryRecommendResponse[] | 이수구분별 추천 결과 |

#### warnings 코드

| Code | 의미 | 출처 |
|------|------|------|
| `NO_GRADUATION_DATA` | 졸업사정표를 유세인트에서 가져오지 못함 (1-1·미제공 등). 동기화 시 세션에 저장된 값이 그대로 전달됨 | 세션(동기화 단계) |
| `NO_GRADUATION_REPORT` | 졸업사정표가 없어 전기/전필/교필 등 추천을 제공하지 못함. 이 API 호출 시 추가됨 | 추천 API |

- 두 코드 모두 **에러가 아닌 경고**이며, 재수강·교직 등 졸업사정표와 무관한 추천은 이용 가능하다.
- 기획·프론트용 상세 설명: [졸업사정표 경고 가이드](../requirements/졸업사정표_경고_가이드.md)

### CategoryRecommendResponse

| 필드 | 타입 | nullable | 설명 |
|------|------|----------|------|
| `category` | String | X | 이수구분 (`RecommendCategory` enum name) |
| `progress` | Progress | X | 졸업사정 이수 현황. 항상 non-null. 센티널 값은 아래 Progress 참고 |
| `message` | String | O | 안내 메시지. **null이면 정상 (과목 존재)** |
| `userGrade` | Int | O | 사용자 학년 |
| `courses` | RecommendedCourseResponse[] | X | 추천 과목 flat list |
| `lateFields` | String[] | O | 교양필수 전용: 미수강 LATE 분야명 텍스트 |

### Progress

| 필드 | 타입 | 설명 |
|------|------|------|
| `required` | Int | 졸업 요구 학점. 센티널: `-1`=재수강/교직(bar 미표시), `-2`=졸업사정표 없음 |
| `completed` | Int | 현재 이수 학점. 센티널: `-1`=재수강/교직, `-2`=졸업사정표 없음 |
| `satisfied` | Boolean | 충족 여부 |

**센티널 값:**
| required | completed | satisfied | 의미 |
|:---:|:---:|:---:|---|
| `0` | `0` | `true` | 해당 없는 이수구분 (FE에서 숨김) |
| `-1` | `-1` | `false` | 재수강/교직 — progress bar 미표시, 과목은 있을 수 있음 |
| `-2` | `-2` | `false` | 졸업사정표 없음 — 제공 불가, bar 미표시 |

**프론트 해석 우선순위:** `-2`(제공 불가) → `-1`(bar 미표시) → `0,0,true`(해당 없음) → 그 외(정상 bar 표시).

**progress 공통 규약:** `progress`는 이 API뿐 아니라 `GET /api/timetables/{id}/available-general-electives`, `GET /api/timetables/{id}/available-chapels`에서도 **항상 non-null**로 사용됩니다. API마다 필드 구조는 다르나 의미는 "이수현황"으로 동일합니다. → [progress 프론트 가이드](../requirements/progress_프론트_가이드.md)

### RecommendedCourseResponse (과목 카드)

| 필드 | 타입 | nullable | 설명 |
|------|------|----------|------|
| `baseCourseCode` | Long | X | 과목 코드 (8자리, 분반 제외) |
| `courseName` | String | X | 과목명 |
| `credits` | Double | O | 학점 |
| `target` | String | X | 수강대상 텍스트 |
| `targetGrades` | Int[] | X | 대상 학년 (e.g., `[1, 2]`) |
| `isCrossMajor` | Boolean | X | 타전공인정 과목 여부. 전공선택에서 타전공 그룹 분리용 |
| `timing` | String | O | `"LATE"` / `"ON_TIME"`. 재수강은 null |
| `field` | String | O | 카테고리 내 하위 그룹 키. 아래 표 참고 |
| `professors` | String[] | X | 교수 목록 (전 분반 합산, 중복제거) |
| `department` | String | O | 개설학과 |
| `sections` | SectionResponse[] | X | 분반 상세 목록 |

### SectionResponse (분반 상세 — 카드 클릭 시)

| 필드 | 타입 | nullable | 설명 |
|------|------|----------|------|
| `courseCode` | Long | X | 과목 코드 (10자리, 분반 포함) |
| `professor` | String | O | 분반 교수 |
| `division` | String | O | 과목코드(10자리)의 마지막 2자리 (e.g. `"01"`, `"02"`) |
| `schedule` | String | X | 강의시간 (e.g., `"월09:00-10:15, 수09:00-10:15"`) |
| `isStrictRestriction` | Boolean | X | 대상외수강제한 여부 |

---

## 과목별 그룹핑 메타데이터

백엔드는 `courses`를 **flat array**로 전달합니다.
각 과목에 그룹핑에 필요한 모든 정보가 포함되어 있습니다.

| 그룹핑 기준 | 사용 필드 | 해당 카테고리 |
|---|---|---|
| 학년별 그룹 | `targetGrades` | MAJOR_ELECTIVE, DOUBLE_MAJOR_ELECTIVE |
| 타전공 구분 | `isCrossMajor` | MAJOR_ELECTIVE |
| 하위 분류 그룹 | `field` | GENERAL_REQUIRED, DOUBLE_MAJOR_REQUIRED, DOUBLE_MAJOR_ELECTIVE, MINOR, TEACHING |
| LATE/ON_TIME 구분 | `timing` | 전체 (재수강 제외) |
| 현재 학년 | `userGrade` (category 레벨) | MAJOR_ELECTIVE, DOUBLE_MAJOR_ELECTIVE |
| LATE 분야 안내 | `lateFields` (category 레벨) | GENERAL_REQUIRED |

### `field` 값 매트릭스

| 카테고리 | `field` 값 예시 | 설명 |
|---|---|---|
| GENERAL_REQUIRED | `"SW와AI"`, `"창의적사고와혁신"` | 교양 분야명 |
| DOUBLE_MAJOR_REQUIRED | `"복필"` | 복수전공필수 |
| DOUBLE_MAJOR_ELECTIVE | `"복선"` | 복수전공선택 |
| MINOR | `"부필"`, `"부선"` | 부전공 이수구분 |
| TEACHING | `"전공"`, `"교직"`, `"특성화"` | 교직 영역 |
| 그 외 | `null` | 사용하지 않음 |

---

## 엣지케이스 처리

**프론트 분기 로직:**
1. `warnings`에 `"NO_GRADUATION_REPORT"` 포함 → 졸업사정표 로딩 불가 경고 표시
2. `progress.required == 0 && satisfied == true` → 해당 없는 이수구분 → 숨김
3. `progress.required == -1` → 재수강/교직 → progress bar 미표시, courses/message만 렌더링
4. `progress.required == -2` → 졸업사정표 없음 → 이수현황 로딩 불가 안내
5. `message != null` → 안내 배너 표시 (이미 이수 / 개설 없음)
6. `message == null` → 과목 카드 렌더링

상세: `recommend_edge_cases.md` 참고

### 1. 이수구분 이미 충족 (satisfied=true)

```json
{
  "category": "MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 18, "satisfied": true },
  "message": "전공필수 학점을 이미 모두 이수하셨습니다.",
  "userGrade": null,
  "courses": [],
  "lateFields": null
}
```

### 2. 미충족이나 이번 학기 개설과목 없음 (satisfied=false)

```json
{
  "category": "MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 12, "satisfied": false },
  "message": "이번 학기에 수강 가능한 전공필수 과목이 없습니다.",
  "userGrade": null,
  "courses": [],
  "lateFields": null
}
```

---

## 정상 응답 예시

### 전공필수

```json
{
  "category": "MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 12, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 21500123,
      "courseName": "자료구조",
      "credits": 3.0,
      "target": "소프트웨어학부 2학년",
      "targetGrades": [2],
      "isCrossMajor": false,
      "timing": "LATE",
      "field": null,
      "professors": ["김교수", "이교수"],
      "department": "소프트웨어학부",
      "sections": [
        {
          "courseCode": 2150012301,
          "professor": "김교수",
          "division": "01",
          "schedule": "월09:00-10:15, 수09:00-10:15",
          "isStrictRestriction": false
        },
        {
          "courseCode": 2150012302,
          "professor": "이교수",
          "division": "02",
          "schedule": "화10:30-11:45, 목10:30-11:45",
          "isStrictRestriction": false
        }
      ]
    },
    {
      "baseCourseCode": 21500456,
      "courseName": "운영체제",
      "credits": 3.0,
      "target": "소프트웨어학부 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": null,
      "professors": ["박교수"],
      "department": "소프트웨어학부",
      "sections": [
        {
          "courseCode": 2150045601,
          "professor": "박교수",
          "division": "01",
          "schedule": "화13:00-14:15, 목13:00-14:15",
          "isStrictRestriction": true
        }
      ]
    }
  ],
  "lateFields": null
}
```

### 전공선택

```json
{
  "category": "MAJOR_ELECTIVE",
  "progress": { "required": 30, "completed": 15, "satisfied": false },
  "message": null,
  "userGrade": 3,
  "courses": [
    {
      "baseCourseCode": 21500111,
      "courseName": "인공지능",
      "credits": 3.0,
      "target": "소프트웨어학부 2학년",
      "targetGrades": [2],
      "isCrossMajor": false,
      "timing": "LATE",
      "field": null,
      "professors": ["한교수"],
      "department": "소프트웨어학부",
      "sections": [...]
    },
    {
      "baseCourseCode": 21500222,
      "courseName": "컴파일러",
      "credits": 3.0,
      "target": "소프트웨어학부 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": null,
      "professors": ["송교수"],
      "department": "소프트웨어학부",
      "sections": [...]
    },
    {
      "baseCourseCode": 21600333,
      "courseName": "경영학원론",
      "credits": 3.0,
      "target": "경영학부 전학년",
      "targetGrades": [1, 2, 3, 4],
      "isCrossMajor": true,
      "timing": "ON_TIME",
      "field": null,
      "professors": ["강교수"],
      "department": "경영학부",
      "sections": [...]
    }
  ],
  "lateFields": null
}
```

프론트 렌더링 (userGrade=3 기준):
```
├── 1학년   (해당 과목 없음)
├── 2학년   (timing=LATE)
│   └── 인공지능
├── 3학년   (timing=ON_TIME) ← 현재 학년
│   └── 컴파일러
├── 4~5학년 (해당 과목 없음)
└── 타전공   (isCrossMajor=true)
    └── 경영학원론
```

### 교양필수

```json
{
  "category": "GENERAL_REQUIRED",
  "progress": { "required": 12, "completed": 3, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 10100001,
      "courseName": "컴퓨팅사고력",
      "credits": 3.0,
      "target": "전교생 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "SW와AI",
      "professors": ["정교수"],
      "department": null,
      "sections": [
        {
          "courseCode": 1010000101,
          "professor": "정교수",
          "division": "01",
          "schedule": "월15:00-16:15, 수15:00-16:15",
          "isStrictRestriction": false
        }
      ]
    },
    {
      "baseCourseCode": 10100005,
      "courseName": "디자인씽킹",
      "credits": 2.0,
      "target": "전교생 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "창의적사고와혁신",
      "professors": ["윤교수"],
      "department": null,
      "sections": [...]
    }
  ],
  "lateFields": ["글로벌시민의식", "글로벌소통과언어"]
}
```

프론트 렌더링:
```
├── [LATE 분야 안내] "글로벌시민의식", "글로벌소통과언어"
├── SW와AI
│   └── 컴퓨팅사고력
└── 창의적사고와혁신
    └── 디자인씽킹
```

### 복수전공필수

```json
{
  "category": "DOUBLE_MAJOR_REQUIRED",
  "progress": { "required": 18, "completed": 6, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 21600100,
      "courseName": "경영학원론",
      "credits": 3.0,
      "target": "경영학부 1학년",
      "targetGrades": [1],
      "isCrossMajor": false,
      "timing": "LATE",
      "field": "복필",
      "professors": ["김교수"],
      "department": "경영학부",
      "sections": [
        {
          "courseCode": 2160010001,
          "professor": "김교수",
          "division": "01",
          "schedule": "월09:00-10:15, 수09:00-10:15",
          "isStrictRestriction": false
        }
      ]
    }
  ],
  "lateFields": null
}
```

### 복수전공선택

```json
{
  "category": "DOUBLE_MAJOR_ELECTIVE",
  "progress": { "required": 18, "completed": 3, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 21600200,
      "courseName": "마케팅원론",
      "credits": 3.0,
      "target": "경영학부 2학년",
      "targetGrades": [2],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "복선",
      "professors": ["박교수"],
      "department": "경영학부",
      "sections": [...]
    }
  ],
  "lateFields": null
}
```

### 부전공

```json
{
  "category": "MINOR",
  "progress": { "required": 21, "completed": 6, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 21700100,
      "courseName": "회계원리",
      "credits": 3.0,
      "target": "경영학부 1학년",
      "targetGrades": [1],
      "isCrossMajor": false,
      "timing": "LATE",
      "field": "부필",
      "professors": ["이교수"],
      "department": "경영학부",
      "sections": [...]
    },
    {
      "baseCourseCode": 21700200,
      "courseName": "재무관리",
      "credits": 3.0,
      "target": "경영학부 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "부선",
      "professors": ["최교수"],
      "department": "경영학부",
      "sections": [...]
    }
  ],
  "lateFields": null
}
```

### 교직이수

```json
{
  "category": "TEACHING",
  "progress": { "required": 22, "completed": 8, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 30100001,
      "courseName": "교육학개론",
      "credits": 2.0,
      "target": "교직이수자 2학년",
      "targetGrades": [2],
      "isCrossMajor": false,
      "timing": "LATE",
      "field": "교직",
      "professors": ["정교수"],
      "department": null,
      "sections": [...]
    },
    {
      "baseCourseCode": 30100002,
      "courseName": "교육실습",
      "credits": 2.0,
      "target": "교직이수자 4학년",
      "targetGrades": [4],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "교직",
      "professors": ["한교수"],
      "department": null,
      "sections": [...]
    },
    {
      "baseCourseCode": 21500999,
      "courseName": "소프트웨어공학",
      "credits": 3.0,
      "target": "소프트웨어학부 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "전공",
      "professors": ["송교수"],
      "department": "소프트웨어학부",
      "sections": [...]
    },
    {
      "baseCourseCode": 30200001,
      "courseName": "다문화교육의이해",
      "credits": 2.0,
      "target": "교직이수자 3학년",
      "targetGrades": [3],
      "isCrossMajor": false,
      "timing": "ON_TIME",
      "field": "특성화",
      "professors": ["윤교수"],
      "department": null,
      "sections": [...]
    }
  ],
  "lateFields": null
}
```

프론트 렌더링 (`field`로 groupBy):
```
├── 전공
│   └── 소프트웨어공학 (ON_TIME)
├── 교직
│   ├── 교육학개론 (LATE)
│   └── 교육실습 (ON_TIME)
└── 특성화
    └── 다문화교육의이해 (ON_TIME)
```

### 재수강

```json
{
  "category": "RETAKE",
  "progress": { "required": -1, "completed": -1, "satisfied": false },
  "message": null,
  "userGrade": null,
  "courses": [
    {
      "baseCourseCode": 21500789,
      "courseName": "미적분학",
      "credits": 3.0,
      "target": "공과대 1학년",
      "targetGrades": [1],
      "isCrossMajor": false,
      "timing": null,
      "field": null,
      "professors": ["최교수"],
      "department": "수학과",
      "sections": [
        {
          "courseCode": 2150078901,
          "professor": "최교수",
          "division": "01",
          "schedule": "화09:00-10:15, 목09:00-10:15",
          "isStrictRestriction": false
        }
      ]
    }
  ],
  "lateFields": null
}
```

---

## TypeScript 타입 참고

```typescript
interface CourseRecommendationsResponse {
  warnings: string[];
  categories: CategoryRecommendResponse[];
}

interface CategoryRecommendResponse {
  category: RecommendCategory;
  /** 센티널: required=-1 → 재수강/교직(bar 미표시), -2 → 졸업사정표 없음 */
  progress: Progress;
  message: string | null;
  userGrade: number | null;
  courses: RecommendedCourse[];
  lateFields: string[] | null;
}

interface Progress {
  required: number;
  completed: number;
  satisfied: boolean;
}

interface RecommendedCourse {
  baseCourseCode: number;
  courseName: string;
  credits: number | null;
  target: string;
  targetGrades: number[];
  isCrossMajor: boolean;
  timing: 'LATE' | 'ON_TIME' | null;
  /** 하위 그룹 키. 교양필수=분야명, 복수전공=복필/복선, 부전공=부필/부선, 교직=전공/교직/특성화 */
  field: string | null;
  professors: string[];
  department: string | null;
  sections: Section[];
}

interface Section {
  courseCode: number;
  professor: string | null;
  /** 과목코드(10자리)의 마지막 2자리 (e.g. "01", "02") */
  division: string | null;
  schedule: string;
  isStrictRestriction: boolean;
}

type RecommendCategory =
  | 'MAJOR_BASIC'
  | 'MAJOR_REQUIRED'
  | 'MAJOR_ELECTIVE'
  | 'GENERAL_REQUIRED'
  | 'RETAKE'
  | 'DOUBLE_MAJOR_REQUIRED'
  | 'DOUBLE_MAJOR_ELECTIVE'
  | 'MINOR'
  | 'TEACHING';
```
