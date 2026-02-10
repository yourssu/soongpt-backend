# Backend DB ERD

`src/main/kotlin/com/yourssu/soongpt/domain/**/storage/*Entity.kt` 기준으로 정리한 ERD입니다.

```mermaid
erDiagram
    COLLEGE ||--o{ DEPARTMENT : "college_id"
    COURSE ||--o{ COURSE_TIME : "course_code"
    COURSE ||--o| COURSE_FIELD : "course_code (unique)"
    COURSE ||--o{ TARGET : "course_code"
    COLLEGE ||--o{ TARGET : "college_id (nullable)"
    DEPARTMENT ||--o{ TARGET : "department_id (nullable)"
    COURSE ||--o{ RATING : "code"
    COURSE ||--o{ COURSE_SECONDARY_MAJOR_CLASSIFICATION : "course_code"
    DEPARTMENT ||--o{ COURSE_SECONDARY_MAJOR_CLASSIFICATION : "department_id"
    TIMETABLE ||--o{ TIMETABLE_COURSE : "timetable_id"
    COURSE ||--o{ TIMETABLE_COURSE : "course_id -> course.id"
    EQUIVALENCE_GROUP ||--o{ COURSE_EQUIVALENCE : "group_id"
    COURSE ||--o| COURSE_EQUIVALENCE : "course_code (PK)"

    COLLEGE {
        BIGINT id PK
        VARCHAR name UK
        DATETIME create_time
        DATETIME update_time
    }

    DEPARTMENT {
        BIGINT id PK
        VARCHAR name UK
        BIGINT college_id
        DATETIME create_time
        DATETIME update_time
    }

    COURSE {
        BIGINT id PK
        ENUM_STRING category
        VARCHAR sub_category
        TEXT multi_major_category
        VARCHAR field
        BIGINT code UK
        VARCHAR name
        VARCHAR professor
        VARCHAR department
        VARCHAR division
        VARCHAR time
        VARCHAR point
        INT personeel
        TEXT schedule_room
        TEXT target
        DOUBLE credit
    }

    COURSE_TIME {
        BIGINT id PK
        BIGINT course_code
        VARCHAR day_of_week
        INT start_minute
        INT end_minute
        VARCHAR room
    }

    COURSE_FIELD {
        BIGINT id PK
        BIGINT course_code UK
        VARCHAR course_name
        VARCHAR field
    }

    TARGET {
        BIGINT id PK
        BIGINT course_code
        ENUM_ORDINAL scope_type
        BIGINT college_id
        BIGINT department_id
        BOOLEAN grade1
        BOOLEAN grade2
        BOOLEAN grade3
        BOOLEAN grade4
        BOOLEAN grade5
        BOOLEAN is_denied
        ENUM_ORDINAL student_type
        BOOLEAN is_strict
    }

    RATING {
        BIGINT id PK
        BIGINT code
        DOUBLE star
    }

    COURSE_SECONDARY_MAJOR_CLASSIFICATION {
        BIGINT id PK
        BIGINT course_code
        ENUM_STRING track_type
        ENUM_STRING completion_type
        BIGINT department_id
        VARCHAR raw_classification
        VARCHAR raw_department_token
    }

    TIMETABLE {
        BIGINT id PK
        ENUM_STRING tag
        INT score
    }

    TIMETABLE_COURSE {
        BIGINT id PK
        BIGINT timetable_id
        BIGINT course_id
    }

    EQUIVALENCE_GROUP {
        BIGINT id PK
        VARCHAR name
        TEXT description
        DATETIME create_time
        DATETIME update_time
    }

    COURSE_EQUIVALENCE {
        BIGINT course_code PK
        BIGINT group_id
        DATETIME create_time
        DATETIME update_time
    }

    CONTACT {
        BIGINT id PK
        TEXT content
        DATETIME create_time
        DATETIME update_time
    }
```

## 제약조건 메모

- `course.code`는 유니크입니다.
- `course_field.course_code`는 유니크여서 과목당 field 레코드는 최대 1개입니다.
- `course_secondary_major_classification`은 `(course_code, track_type, completion_type, department_id)` 복합 유니크 제약이 있습니다.
- `course_equivalence.course_code`가 PK이므로 과목은 최대 하나의 equivalence group에 속합니다.
- `target.college_id`, `target.department_id`는 nullable이며 `scope_type` 조합으로 의미가 결정됩니다.
- `college`, `department`, `equivalence_group`, `course_equivalence`, `contact`는 감사 컬럼(`create_time`, `update_time`)을 가집니다.
