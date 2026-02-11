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
- `teachingArea` (string, optional)
  - 교직 영역 (없으면 전체 교직 과목 반환)
  - 허용값
    - `교직이론` 또는 `THEORY`
    - `교직소양` 또는 `LITERACY`
    - `교육실습` 또는 `PRACTICE`
    - `교과교육` 또는 `SUBJECT_EDUCATION`

---

## Examples

### 1) 교직 과목 전체 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부
```

### 2) 교직소양만 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부&teachingArea=교직소양
```

---

## Response

### 200 OK

- `Response<List<CourseResponse>>`
  - `CourseResponse`는 과목 기본정보 + 강의시간 리스트를 포함합니다.
