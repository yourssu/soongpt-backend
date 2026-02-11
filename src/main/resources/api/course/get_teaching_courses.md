# 교직 과목 조회 (GET /api/courses/teaching)

특정 학과 기준으로 교직 과목을 조회합니다.

- 조회 대상은 `course.category = TEACHING` 과목입니다.
- 학년(1~5학년) 전체를 대상으로 조회합니다.

---

## Request

### Query Parameters

- `schoolId` (number, required)
  - 학번 앞 2자리 (예: `26`)
- `department` (string, required)
  - 학과명 (예: `컴퓨터학부`)
- `majorArea` (string, optional)
  - 교직 대분류 영역 (없으면 전체 교직 과목 반환)
  - 허용값
    - `전공영역` 또는 `MAJOR` — 전공영역 과목 (교과교육론, 논리및논술 등)
    - `교직영역` 또는 `TEACHING` — 교직영역 과목 (교직이론, 교육실습 등)
    - `특성화영역` 또는 `SPECIAL` — 특성화영역 과목

---

## Examples

### 1) 교직 과목 전체 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부
```

### 2) 전공영역만 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부&majorArea=MAJOR
```

### 3) 교직영역만 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부&majorArea=TEACHING
```

---

## Response

### 200 OK

- `Response<List<CourseResponse>>`
  - `CourseResponse`는 과목 기본정보 + 강의시간 리스트를 포함합니다.
