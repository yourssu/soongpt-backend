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
  - 교직 이수 관점 **대분류**
  - 허용값
    - `전공영역` 또는 `MAJOR`
    - `교직영역` 또는 `TEACHING`
    - `특성화` 또는 `SPECIALIZATION`
  - 동작
    - 지정 시 `course.field` 가 해당 prefix로 시작하는 과목만 반환
      - 전공영역 → `전공영역/`
      - 교직영역 → `교직영역/`
      - 특성화 → `특성화/`
- `teachingArea` (string, optional)
  - (하위호환) 기존 교직 영역 필터
  - 허용값
    - `교직이론` 또는 `THEORY`
    - `교직소양` 또는 `LITERACY`
    - `교육실습` 또는 `PRACTICE`
    - `교과교육` 또는 `SUBJECT_EDUCATION`

> 참고: `majorArea` 와 `teachingArea` 를 동시에 사용하면 **majorArea로 1차 필터링 후**, teachingArea로 2차 필터링합니다.

---

## Examples

### 1) 교직 과목 전체 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부
```

### 2) 교직영역(교직이론/소양/교육실습)만 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부&majorArea=교직영역
```

### 3) 전공영역(교과교육영역)만 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부&majorArea=전공영역
```

### 4) (하위호환) 기존 teachingArea로 교직소양만 조회

```http
GET /api/courses/teaching?schoolId=26&department=컴퓨터학부&teachingArea=교직소양
```

---

## Response

### 200 OK

- `Response<List<CourseResponse>>`
  - `CourseResponse`는 과목 기본정보 + 강의시간 리스트를 포함합니다.

---

## Notes

- `course.field` 값은 다음 형태를 권장합니다.
  - `전공영역/교과교육영역`
  - `교직영역/교직이론`
  - `교직영역/교직소양`
  - `교직영역/교육실습`
  - `특성화/특성화`
