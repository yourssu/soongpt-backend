# searchCourse (GET /api/courses/search)

## 개요

키워드로 강의를 검색합니다. 검색어가 비어 있으면 전체 강의 목록을 페이지네이션하여 반환합니다.
동일 과목(동일 baseCourseCode)의 분반들은 하나의 그룹(`SearchCourseGroupResponse`)으로 묶어서 반환합니다.

---

## Request

### Query Parameters

| Name     | Type    | Required | Constraint                      | Description                |
| -------- | ------- | -------- | ------------------------------- | -------------------------- |
| `q`    | string  | false    |                                 | 검색어 (기본값: 빈 문자열) |
| `page` | integer | false    | 0 ≤ value                      | 페이지 번호 (기본값: 0)    |
| `size` | integer | false    | 1 ≤ value ≤ 100               | 페이지 크기 (기본값: 20)   |
| `sort` | string  | false    | ASC\| DESC (대소문자 구분 없음) | 정렬 방식 (기본값: ASC)    |

- **검색어(q) 허용 패턴**: `[가-힣a-zA-ZⅠ-Ⅹ0-9\s\n().,;:_-]` (이외 문자는 제거됨)
- **검색어 최대 길이**: 50자

### 검색 대상 필드 (FULLTEXT + code)

- `name` (과목명)
- `professor` (교수명)
- `department` (학과)
- `target` (수강 대상)
- `schedule_room` (시간/장소)
- `code` (과목 코드, 접두사 일치)

### 사용 예시

```
GET /api/courses/search?q=김남미              # 교수명으로 검색
GET /api/courses/search?q=물리1               # 과목명으로 검색
GET /api/courses/search?q=Business+English    # 영문 과목명 검색
GET /api/courses/search?q=&page=1&size=50     # 빈 검색어 + 페이지네이션
GET /api/courses/search?page=0&size=20        # 전체 강의 목록 조회
```

---

## Reply

### Response Body

| Name            | Type                        | Nullable | Description                 |
| --------------- | --------------------------- | -------- | --------------------------- |
| `courses`       | SearchCourseGroupResponse[] | No       | 과목 그룹(분반 묶음) 배열   |
| `totalElements` | long                        | No       | 전체 요소 개수              |
| `totalPages`    | integer                     | No       | 전체 페이지 수              |
| `size`          | integer                     | No       | 페이지당 요소 개수          |
| `page`          | integer                     | No       | 현재 페이지 번호 (0-based)  |

### SearchCourseGroupResponse (과목 그룹)

동일 과목(baseCourseCode)의 여러 분반을 하나로 묶은 단위입니다.

| Name             | Type              | Nullable | Description                            |
| ---------------- | ----------------- | -------- | -------------------------------------- |
| `baseCourseCode` | long              | No       | 과목 기준 코드 (분반 제외, 8자리 등)   |
| `courseName`     | string            | No       | 과목명                                 |
| `credits`        | number            | Yes      | 학점                                   |
| `professors`     | string[]          | No       | 담당 교수 목록 (분반별 교수 중복 제거) |
| `department`     | string            | No       | 개설 학과                              |
| `sections`       | SectionResponse[] | No       | 분반별 상세 (분반 코드, 교수, 시간 등) |

(참고: 채플 과목은 검색 결과에서 제외됩니다.)

### SectionResponse (분반 정보)

| Name                 | Type    | Nullable | Description                                                                 |
| -------------------- | ------- | -------- | --------------------------------------------------------------------------- |
| `courseCode`         | long    | No       | 분반별 과목 코드                                                            |
| `professor`          | string  | Yes      | 해당 분반 담당 교수                                                         |
| `division`           | string  | No       | 분반 번호 (courseCode 뒷 2자리, 예: 2150533504 → "04", 2150533501 → "01")   |
| `schedule`           | string  | No       | 시간 정보 (장소 제외, 쉼표/줄바꿈 구분)                                     |
| `isStrictRestriction`| boolean | No       | 수강 제한 엄격 여부                                                         |

---

### 200 OK 예시

```json
{
  "timestamp": "2025-06-04 08:46:52",
  "result": {
    "courses": [
      {
        "baseCourseCode": 21505335,
        "courseName": "Art & Technology",
        "credits": 3,
        "professors": ["전석", "정기철"],
        "department": "글로벌미디어학부",
        "sections": [
          {
            "courseCode": 2150533504,
            "professor": "전석",
            "division": "04",
            "schedule": "월 10:30-11:45, 월 12:00-13:15",
            "isStrictRestriction": false
          },
          {
            "courseCode": 2150533501,
            "professor": "정기철",
            "division": "01",
            "schedule": "화 09:00-10:15, 목 09:00-10:15",
            "isStrictRestriction": false
          },
          {
            "courseCode": 2150533502,
            "professor": "정기철",
            "division": "02",
            "schedule": "화 10:30-11:45, 목 10:30-11:45",
            "isStrictRestriction": false
          },
          {
            "courseCode": 2150533503,
            "professor": "정기철",
            "division": "03",
            "schedule": "화 13:30-14:45, 목 13:30-14:45",
            "isStrictRestriction": false
          }
        ]
      }
    ],
    "totalElements": 120,
    "totalPages": 6,
    "size": 20,
    "page": 0
  }
}
```

---

## 구현 참고 (검색·정렬)

- **검색**: MySQL FULLTEXT (`name`, `professor`, `department`, `target`, `schedule_room`) BOOLEAN MODE + `code` 접두사 일치.
- **정렬 우선순위**: 과목 코드 정확 일치 → 코드 접두사 일치 → 과목명 접두사 → 교수명 접두사 → 학과명 접두사 → 과목명 길이 → 과목명 오름차순.
- **채플**: `CHAPEL` 이수 구분 과목은 검색 결과에서 제외됨.
- **그룹핑**: 동일 `baseCourseCode`(과목 기준 코드)의 분반들을 한 그룹으로 묶어 반환됨. `sections[].division`은 과목 코드(courseCode) 뒷 2자리로 내려감 (예: 2150533504 → "04").
