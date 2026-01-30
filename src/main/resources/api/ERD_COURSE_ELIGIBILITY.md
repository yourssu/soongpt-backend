# ERD: Proposed Schema (aligned with backend names)

```mermaid
erDiagram
    college ||--o{ department : "단과대학-학과"
    course ||--o{ target : "수강 대상"
    course ||--o{ course_time : "강의 시간"
    department ||--o{ target : "학과 대상"

    college {
        Long id PK "단과대학 ID"
        String name UK "단과대학명"
    }

    department {
        Long id PK "학과 ID"
        String name UK "학과명"
        Long collegeId "단과대학 ID(FK)"
    }

    course {
        Long id PK
        Category category "과목 분류(전필/전선/교필 등)"
        String subCategory "세부 분류(원본 데이터 기반)"
        String field "필드/영역(학교/학과별 필터링에 사용)"
        Long code UK "과목코드"
        String name "과목명"
        String professor "교수명"
        String department "개설학과/주관학과(문자열)"
        String division "구분 정보(원본 데이터 기반)"
        String time "시간 관련 원본 필드(문자열)"
        String point "학점/이수학점 관련 원본 필드(문자열)"
        Int personeel "수강 인원(정원)"
        String scheduleRoom "시간+강의실 원본 문자열"
        String target "수강대상 원본 문자열"

        Double credit "학점(추가)"
        String area "영역/교양구분(추가)"
        Boolean isTeachingCert "교직이수 여부(추가)"
    }

    course_time {
        Long id PK
        Long courseCode FK "과목 코드"
        String dayOfWeek "요일(MON~SUN)"
        Int startMinute "시작시간(분 단위)"
        Int endMinute "종료시간(분 단위)"
        String room "강의실"
    }

    target {
        Long id PK
        Long departmentId FK "학과 ID"
        Long courseCode FK "과목 코드"
        Int grade "학년(1~5)"
        Boolean isExcluded "대상 외 수강제한"
        Boolean isForeignerOnly "외국인만 수강 가능"
    }
```

## Index Notes

- Recommended (DB): `INDEX (department_id, grade, course_code)` for `WHERE department_id=? AND grade=?` + `SELECT course_code` (covering)
- Maps to (entity): `TargetEntity.departmentId`, `TargetEntity.grade`, `TargetEntity.courseCode`

- Recommended (DB): `INDEX (course_code)` for `course_time` lookup by course
