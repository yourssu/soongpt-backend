# ERD: Proposed Schema (aligned with backend names)

```mermaid
erDiagram
    college ||--o{ department : "단과대학-학과"
    college ||--o{ target : "단과대 범위 수강대상"
    department ||--o{ target : "학과 범위 수강대상"
    course ||--o{ target : "수강 대상"
    course ||--o{ course_time : "강의 시간"
    equivalence_group ||--o{ course_equivalence : "동일 대체 그룹"
    course ||--o| course_equivalence : "동일 대체 관계"

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
        String target "수강대상 원본 문자열(읽기 전용, 감사용)"

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
        Long id PK "수강대상 ID"
        Long courseCode FK "과목 코드"
        ScopeType scopeType "범위 타입(UNIVERSITY/COLLEGE/DEPARTMENT)"
        Long collegeId FK "단과대학 ID(NULL 가능)"
        Long departmentId FK "학과 ID(NULL 가능)"
        Int minGrade "최소 학년(1~5)"
        Int maxGrade "최대 학년(1~5)"
        Boolean isExcluded "대상 외 수강제한"
        Boolean isForeignerOnly "외국인만 수강 가능"
    }

    equivalence_group {
        Long id PK "동일 대체 과목 그룹 ID"
        String name "그룹명(선택)"
        String description "그룹 설명(선택)"
        DateTime createdAt "생성 시간"
        DateTime updatedAt "수정 시간"
    }

    course_equivalence {
        Long courseCode PK_FK "과목 코드"
        Long groupId FK "그룹 ID"
        DateTime createdAt "등록 시간"
    }
```

## Schema Details

### target 테이블 제약조건

```sql
CREATE TABLE target (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    course_code BIGINT NOT NULL,

    -- 범위 지정 (상호 배타적)
    scope_type ENUM('UNIVERSITY', 'COLLEGE', 'DEPARTMENT') NOT NULL,
    college_id BIGINT NULL,
    department_id BIGINT NULL,

    -- 학년 범위
    min_grade TINYINT NOT NULL DEFAULT 1,  -- 1~5
    max_grade TINYINT NOT NULL DEFAULT 5,  -- 1~5

    -- 추가 제약
    is_excluded BOOLEAN NOT NULL DEFAULT false,
    is_foreigner_only BOOLEAN NOT NULL DEFAULT false,

    -- 외래키
    FOREIGN KEY (course_code) REFERENCES course(code),
    FOREIGN KEY (college_id) REFERENCES college(id),
    FOREIGN KEY (department_id) REFERENCES department(id),

    -- 무결성 제약조건
    CONSTRAINT chk_scope CHECK (
        (scope_type = 'UNIVERSITY' AND college_id IS NULL AND department_id IS NULL)
        OR (scope_type = 'COLLEGE' AND college_id IS NOT NULL AND department_id IS NULL)
        OR (scope_type = 'DEPARTMENT' AND department_id IS NOT NULL)
    ),
    CONSTRAINT chk_grade_range CHECK (min_grade >= 1 AND max_grade <= 5 AND min_grade <= max_grade)
);
```

### ScopeType ENUM 정의

```kotlin
enum class ScopeType {
    UNIVERSITY,   // 전교생 대상
    COLLEGE,      // 단과대학 범위
    DEPARTMENT    // 학과 범위
}
```

## Index Strategy

### 인덱스 정의
```sql
-- 학과 기준 검색 (가장 빈번한 쿼리)
CREATE INDEX idx_target_dept_grade
ON target(department_id, min_grade, max_grade, course_code);

-- 단과대 기준 검색
CREATE INDEX idx_target_college_grade
ON target(college_id, min_grade, max_grade, course_code);

-- 과목 코드 기준 검색
CREATE INDEX idx_target_course
ON target(course_code);

-- 범위 타입 + 학년 검색 (전교생 과목 조회 등)
CREATE INDEX idx_target_scope_grade
ON target(scope_type, min_grade, max_grade);

-- course_time 테이블
CREATE INDEX idx_course_time_course
ON course_time(course_code);
```

## Query Examples

### 1. 컴퓨터학부 2학년 수강 가능 과목 조회
```sql
SELECT DISTINCT c.*
FROM course c
JOIN target t ON c.code = t.course_code
WHERE (
    t.scope_type = 'UNIVERSITY'  -- 전교생 대상
    OR (t.scope_type = 'COLLEGE' AND t.college_id = :collegeId)  -- 소속 단과대
    OR (t.scope_type = 'DEPARTMENT' AND t.department_id = :deptId)  -- 소속 학과
)
AND 2 BETWEEN t.min_grade AND t.max_grade  -- 학년 범위 확인
AND t.is_excluded = false;
```

### 2. 특정 과목의 수강 대상 확인
```sql
SELECT
    t.scope_type,
    CASE
        WHEN t.scope_type = 'UNIVERSITY' THEN '전교생'
        WHEN t.scope_type = 'COLLEGE' THEN c.name
        WHEN t.scope_type = 'DEPARTMENT' THEN d.name
    END AS target_name,
    CONCAT(t.min_grade, '~', t.max_grade, '학년') AS grade_range,
    t.is_excluded,
    t.is_foreigner_only
FROM target t
LEFT JOIN college c ON t.college_id = c.id
LEFT JOIN department d ON t.department_id = d.id
WHERE t.course_code = :courseCode;
```

### 3. 외국인 전용 과목 조회
```sql
SELECT c.*
FROM course c
JOIN target t ON c.code = t.course_code
WHERE t.is_foreigner_only = true;
```

## Data Examples

### 전교생 대상 교양 과목
```sql
INSERT INTO target (course_code, scope_type, min_grade, max_grade)
VALUES (12345, 'UNIVERSITY', 1, 5);
```

### 공과대학 전체 대상
```sql
INSERT INTO target (course_code, scope_type, college_id, min_grade, max_grade)
VALUES (23456, 'COLLEGE', 1, 1, 5);
```

### 컴퓨터학부 2학년 전공
```sql
INSERT INTO target (course_code, scope_type, department_id, min_grade, max_grade)
VALUES (34567, 'DEPARTMENT', 10, 2, 2);
```

### 외국인만 수강 가능한 과목
```sql
INSERT INTO target (course_code, scope_type, min_grade, max_grade, is_foreigner_only)
VALUES (45678, 'UNIVERSITY', 1, 5, true);
```

### 컴퓨터학부 제외 전체
```sql
INSERT INTO target (course_code, scope_type, department_id, min_grade, max_grade, is_excluded)
VALUES (56789, 'DEPARTMENT', 10, 1, 5, true);
```

---

## 동일 대체 과목 (Equivalent Courses)

### 개념
동일 대체 과목이란 **서로 다른 과목이지만 같은 과목으로 인정되는 경우**를 의미합니다.
- 예: "자료구조"와 "자료구조론"이 동일 대체 관계
- 예: "미적분학1", "미적분학I", "Calculus I"이 모두 동일 그룹
- 용도: 졸업 요건 충족 시 중복 이수 방지, 학점 인정

### Union-Find 기반 그룹 관리

이 설계는 **Union-Find (Disjoint Set)** 자료구조를 DB로 표현합니다.
- 각 그룹은 독립적인 집합 (Disjoint Set)
- 같은 그룹 내의 모든 과목은 동일 대체 관계
- 효율적인 검색과 그룹 병합 지원

### Schema Details

```sql
-- 동일 대체 과목 그룹
CREATE TABLE equivalence_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NULL COMMENT '그룹명 (예: "자료구조 계열")',
    description TEXT NULL COMMENT '그룹 설명',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 과목-그룹 매핑 (Union-Find의 원소-집합 관계)
CREATE TABLE course_equivalence (
    course_code BIGINT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (course_code) REFERENCES course(code) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES equivalence_group(id) ON DELETE CASCADE,

    -- 인덱스
    INDEX idx_group_id (group_id)
);
```

### 인덱스 전략

```sql
-- 그룹 ID로 모든 동일 대체 과목 조회
CREATE INDEX idx_course_equivalence_group
ON course_equivalence(group_id, course_code);

-- 과목 코드로 그룹 조회 (PK로 자동 생성)
-- PRIMARY KEY (course_code)
```

### Query Examples

#### 1. 특정 과목의 모든 동일 대체 과목 조회
```sql
-- "자료구조" 과목(20001)과 동일한 과목 모두 찾기
SELECT c.*
FROM course c
JOIN course_equivalence ce ON c.code = ce.course_code
WHERE ce.group_id = (
    SELECT group_id
    FROM course_equivalence
    WHERE course_code = 20001
)
AND ce.course_code != 20001;  -- 자기 자신 제외
```

**결과 예시:**
| code | name |
|------|------|
| 20002 | 자료구조론 |
| 20003 | Data Structures |

#### 2. 학생이 이미 이수한 과목과 동일한 과목 체크 (중복 이수 방지)
```sql
-- 학생이 수강하려는 과목(30001)이 이미 이수한 과목과 동일한지 확인
SELECT EXISTS(
    SELECT 1
    FROM course_equivalence ce1
    JOIN course_equivalence ce2 ON ce1.group_id = ce2.group_id
    WHERE ce1.course_code = 30001  -- 수강하려는 과목
      AND ce2.course_code IN (20001, 20002)  -- 이미 이수한 과목들
) AS is_duplicate;
```

#### 3. 동일 대체 그룹 전체 조회 (관리자용)
```sql
SELECT
    eg.id AS group_id,
    eg.name AS group_name,
    GROUP_CONCAT(c.name ORDER BY c.code SEPARATOR ', ') AS courses
FROM equivalence_group eg
JOIN course_equivalence ce ON eg.id = ce.group_id
JOIN course c ON ce.course_code = c.code
GROUP BY eg.id, eg.name
ORDER BY eg.id;
```

**결과 예시:**
| group_id | group_name | courses |
|----------|------------|---------|
| 1 | 자료구조 계열 | 자료구조, 자료구조론, Data Structures |
| 2 | 미적분학 계열 | 미적분학1, 미적분학I, Calculus I |

#### 4. Union (두 그룹 병합)
```sql
-- 그룹 2의 모든 과목을 그룹 1로 병합
START TRANSACTION;

-- 그룹 2의 과목들을 그룹 1로 이동
UPDATE course_equivalence
SET group_id = 1
WHERE group_id = 2;

-- 그룹 2 삭제
DELETE FROM equivalence_group WHERE id = 2;

COMMIT;
```

#### 5. Find (과목이 속한 그룹 찾기)
```sql
-- 과목 코드로 그룹 정보 조회
SELECT eg.*
FROM equivalence_group eg
JOIN course_equivalence ce ON eg.id = ce.group_id
WHERE ce.course_code = 20001;
```

### Data Examples

#### 예시 1: 자료구조 계열 그룹 생성
```sql
-- 1. 그룹 생성
INSERT INTO equivalence_group (name, description)
VALUES ('자료구조 계열', '자료구조 관련 동일 대체 과목');

-- 2. 과목들을 그룹에 추가 (group_id = 1 가정)
INSERT INTO course_equivalence (course_code, group_id) VALUES
(20001, 1),  -- 자료구조
(20002, 1),  -- 자료구조론
(20003, 1);  -- Data Structures
```

#### 예시 2: 미적분학 계열 그룹 생성
```sql
-- 1. 그룹 생성
INSERT INTO equivalence_group (name, description)
VALUES ('미적분학 계열', '미적분학 기초 과목');

-- 2. 과목들을 그룹에 추가 (group_id = 2 가정)
INSERT INTO course_equivalence (course_code, group_id) VALUES
(30001, 2),  -- 미적분학1
(30002, 2),  -- 미적분학I
(30003, 2);  -- Calculus I
```

#### 예시 3: 그룹 병합 (Union 연산)
```sql
-- "자료구조 계열"(1)과 "알고리즘 계열"(3)을 병합
UPDATE course_equivalence
SET group_id = 1
WHERE group_id = 3;

DELETE FROM equivalence_group WHERE id = 3;
```

### Union-Find 연산 매핑

| Union-Find 연산 | DB 구현 |
|-----------------|---------|
| **MakeSet(x)** | `INSERT INTO equivalence_group` + `INSERT INTO course_equivalence` |
| **Find(x)** | `SELECT group_id FROM course_equivalence WHERE course_code = x` |
| **Union(x, y)** | `UPDATE course_equivalence SET group_id = Find(x) WHERE group_id = Find(y)` + `DELETE FROM equivalence_group` |
| **Connected(x, y)** | `Find(x) == Find(y)` (그룹 ID 비교) |

### 사용 시나리오

#### 시나리오 1: 졸업 요건 체크
학생이 "자료구조"를 이수했다면, "자료구조론" 이수로 인정하지 않음
```kotlin
fun hasTakenEquivalentCourse(studentId: Long, courseCode: Long): Boolean {
    val takenCourses = getTakenCourses(studentId)
    val targetGroup = findGroup(courseCode) ?: return false

    return takenCourses.any { course ->
        findGroup(course.code) == targetGroup
    }
}
```

#### 시나리오 2: 수강 신청 중복 체크
동일한 학기에 동일 대체 과목을 여러 개 신청하는 것 방지
```kotlin
fun validateCourseRegistration(courses: List<Long>): ValidationResult {
    val groups = courses.mapNotNull { findGroup(it) }
    val duplicates = groups.groupingBy { it }.eachCount().filter { it.value > 1 }

    return if (duplicates.isEmpty()) {
        ValidationResult.Success
    } else {
        ValidationResult.Failure("동일 대체 과목 중복 신청")
    }
}
```

### 설계 장점

1. **명확한 그룹 관리**: equivalence_group으로 집합 명시
2. **빠른 검색**: `group_id`로 O(1) 동일성 확인
3. **효율적인 그룹 병합**: UPDATE 한 번으로 Union 연산
4. **확장성**: 새로운 과목을 기존 그룹에 추가 용이
5. **데이터 무결성**: FK 제약조건으로 일관성 보장

### 대안: Union-Find 직접 표현 방식

만약 **진짜 Union-Find 트리 구조**를 원한다면:

```sql
CREATE TABLE course_equivalence_uf (
    course_code BIGINT PRIMARY KEY,
    parent_course_code BIGINT NOT NULL,
    rank INT NOT NULL DEFAULT 0,

    FOREIGN KEY (course_code) REFERENCES course(code),
    FOREIGN KEY (parent_course_code) REFERENCES course(code),

    INDEX idx_parent (parent_course_code)
);
```

**장점:**
- Union-Find 알고리즘 그대로 구현
- Path compression 적용 가능

**단점:**
- 루트 찾기에 재귀 쿼리 필요 (CTE)
- 복잡도 높음
- 대부분의 RDBMS에서는 그룹 기반이 더 적합

**추천: 그룹 기반 방식** (현재 설계)을 사용하세요. RDBMS의 장점을 최대한 활용하면서도 Union-Find의 의미를 충분히 표현합니다.

---

## 동일 대체 과목 (Equivalent Courses)

### 개요
동일 대체 과목이란 서로 다른 과목 코드를 가지지만 **같은 과목으로 인정되는 과목들**입니다.
- 예: 구 교육과정 과목 ↔ 신 교육과정 과목
- 예: 학과 통폐합으로 인한 과목 변경
- 특징: **하나만 수강하면 다른 과목들도 이수한 것으로 인정**

### Union-Find 구조 설계

#### 테이블 구조

```sql
-- 동일 대체 과목 그룹 (Union-Find의 루트 노드)
CREATE TABLE equivalence_group (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    root_course_code BIGINT NOT NULL,  -- 대표 과목 (루트)
    description VARCHAR(500),           -- 그룹 설명 (예: "2023 교육과정 변경")
    rank INT NOT NULL DEFAULT 0,       -- Union-Find rank (최적화용)
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (root_course_code) REFERENCES course(code),
    INDEX idx_root_course (root_course_code)
);

-- 그룹 내 과목 관계 (Union-Find의 집합 원소)
CREATE TABLE course_equivalence (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    course_code BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (group_id) REFERENCES equivalence_group(id) ON DELETE CASCADE,
    FOREIGN KEY (course_code) REFERENCES course(code),

    -- 하나의 과목은 하나의 그룹에만 속함
    UNIQUE KEY uk_course_code (course_code),
    INDEX idx_group (group_id, course_code)
);

-- course 테이블에 컬럼 추가
ALTER TABLE course
ADD COLUMN equivalence_group_id BIGINT NULL,
ADD FOREIGN KEY (equivalence_group_id) REFERENCES equivalence_group(id);

CREATE INDEX idx_course_equiv_group ON course(equivalence_group_id);
```

### Union-Find 연산

#### 1. Find (그룹 찾기)
```sql
-- 특정 과목이 속한 그룹 조회
SELECT eg.*
FROM course c
JOIN equivalence_group eg ON c.equivalence_group_id = eg.id
WHERE c.code = :courseCode;

-- 또는 course_equivalence를 통해
SELECT eg.*
FROM course_equivalence ce
JOIN equivalence_group eg ON ce.group_id = eg.id
WHERE ce.course_code = :courseCode;
```

#### 2. Union (그룹 합치기)
```kotlin
// Kotlin 서비스 레이어 예시
@Transactional
fun unionCourses(courseCode1: Long, courseCode2: Long) {
    val group1 = findGroup(courseCode1)
    val group2 = findGroup(courseCode2)

    if (group1 != null && group2 != null) {
        // 이미 둘 다 그룹이 있는 경우: rank 기반 union
        if (group1.rank >= group2.rank) {
            mergeGroups(keep = group1, merge = group2)
            if (group1.rank == group2.rank) group1.rank++
        } else {
            mergeGroups(keep = group2, merge = group1)
        }
    } else if (group1 != null) {
        // group1만 있는 경우: courseCode2를 group1에 추가
        addToGroup(group1, courseCode2)
    } else if (group2 != null) {
        // group2만 있는 경우: courseCode1을 group2에 추가
        addToGroup(group2, courseCode1)
    } else {
        // 둘 다 없는 경우: 새 그룹 생성
        val newGroup = createGroup(courseCode1)
        addToGroup(newGroup, courseCode2)
    }
}

private fun mergeGroups(keep: EquivalenceGroup, merge: EquivalenceGroup) {
    // merge 그룹의 모든 과목을 keep 그룹으로 이동
    courseEquivalenceRepository.updateGroupId(
        fromGroupId = merge.id,
        toGroupId = keep.id
    )

    // merge 그룹 삭제
    equivalenceGroupRepository.delete(merge)
}
```

#### 3. 쿼리 최적화: Path Compression
```kotlin
// 애플리케이션 레벨에서 주기적으로 실행
@Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
fun compressEquivalencePaths() {
    val allGroups = equivalenceGroupRepository.findAll()

    allGroups.forEach { group ->
        // 모든 과목의 equivalence_group_id를 최신화
        courseRepository.updateEquivalenceGroupId(
            courseIds = courseEquivalenceRepository.findCourseCodesByGroupId(group.id),
            groupId = group.id
        )
    }
}
```

### 주요 쿼리 예시

#### 1. 동일 대체 과목 목록 조회
```sql
-- 과목 A와 동일 대체 인정되는 모든 과목 조회
SELECT c.*
FROM course c
WHERE c.equivalence_group_id = (
    SELECT equivalence_group_id
    FROM course
    WHERE code = :courseCodeA
);

-- 또는 course_equivalence를 통해
SELECT c.*
FROM course c
JOIN course_equivalence ce ON c.code = ce.course_code
WHERE ce.group_id = (
    SELECT group_id
    FROM course_equivalence
    WHERE course_code = :courseCodeA
);
```

#### 2. 학생이 동일 대체 과목 중 하나라도 수강했는지 확인
```sql
-- 학생이 이수한 과목 중 equivalence_group_id가 일치하는 것이 있는지
SELECT COUNT(*) > 0 AS has_taken_equivalent
FROM student_enrollment se
JOIN course c ON se.course_code = c.code
WHERE se.student_id = :studentId
  AND c.equivalence_group_id = :targetGroupId;
```

#### 3. 시간표 중복 체크 (동일 대체 과목 고려)
```sql
-- 시간표에 동일 대체 과목이 중복으로 들어가지 않도록
SELECT COUNT(*) AS duplicate_count
FROM timetable_course tc
JOIN course c ON tc.course_code = c.code
WHERE tc.timetable_id = :timetableId
  AND c.equivalence_group_id IS NOT NULL
GROUP BY c.equivalence_group_id
HAVING COUNT(*) > 1;
```

### 데이터 예시

#### 교육과정 변경으로 인한 동일 대체 과목
```sql
-- 1. 그룹 생성 (자료구조 과목 그룹)
INSERT INTO equivalence_group (root_course_code, description, rank)
VALUES (20001, '자료구조 교육과정 변경 (2023)', 0);

SET @group_id = LAST_INSERT_ID();

-- 2. 구 교육과정 과목들을 그룹에 추가
INSERT INTO course_equivalence (group_id, course_code) VALUES
    (@group_id, 20001),  -- 자료구조 (2023)
    (@group_id, 10001),  -- 자료구조론 (2020)
    (@group_id, 10002);  -- 자료구조및실습 (2018)

-- 3. course 테이블 업데이트
UPDATE course
SET equivalence_group_id = @group_id
WHERE code IN (20001, 10001, 10002);
```

#### 학과 통폐합으로 인한 동일 대체 과목
```sql
-- 컴퓨터공학과 + 소프트웨어학과 → 컴퓨터학부 통합
INSERT INTO equivalence_group (root_course_code, description, rank)
VALUES (30001, '학과 통폐합 (컴퓨터학부)', 0);

SET @group_id = LAST_INSERT_ID();

INSERT INTO course_equivalence (group_id, course_code) VALUES
    (@group_id, 30001),  -- 컴퓨터학부 운영체제
    (@group_id, 20011),  -- 컴퓨터공학과 운영체제
    (@group_id, 20012);  -- 소프트웨어학과 운영체제론

UPDATE course
SET equivalence_group_id = @group_id
WHERE code IN (30001, 20011, 20012);
```

### 인덱스 전략

```sql
-- equivalence_group
CREATE INDEX idx_equiv_group_root ON equivalence_group(root_course_code);
CREATE INDEX idx_equiv_group_updated ON equivalence_group(updated_at);

-- course_equivalence
CREATE UNIQUE INDEX uk_course_equiv_code ON course_equivalence(course_code);
CREATE INDEX idx_course_equiv_group ON course_equivalence(group_id, course_code);

-- course
CREATE INDEX idx_course_equiv_group ON course(equivalence_group_id);
```

### 성능 고려사항

1. **이중 참조 구조**
   - `course.equivalence_group_id`: 빠른 그룹 조회 (denormalization)
   - `course_equivalence`: 정확한 관계 관리 (normalization)
   - Trade-off: 저장 공간 vs 조회 속도

2. **Path Compression**
   - DB 제약: 트랜잭션 중 path compression 어려움
   - 해결: 배치 작업으로 주기적으로 `course.equivalence_group_id` 갱신

3. **Union by Rank**
   - `equivalence_group.rank`: 트리 높이 최소화
   - 그룹 병합 시 rank가 높은 쪽을 루트로 유지

### 비즈니스 로직 예시

```kotlin
@Service
class CourseEquivalenceService(
    private val equivalenceGroupRepository: EquivalenceGroupRepository,
    private val courseEquivalenceRepository: CourseEquivalenceRepository,
    private val courseRepository: CourseRepository
) {

    /**
     * 두 과목이 동일 대체 과목인지 확인
     */
    fun areEquivalent(courseCode1: Long, courseCode2: Long): Boolean {
        if (courseCode1 == courseCode2) return true

        val group1 = findGroupByCourseCode(courseCode1)
        val group2 = findGroupByCourseCode(courseCode2)

        return group1 != null && group2 != null && group1.id == group2.id
    }

    /**
     * 과목의 모든 동일 대체 과목 조회
     */
    fun findEquivalentCourses(courseCode: Long): List<Course> {
        val course = courseRepository.findByCode(courseCode)
            ?: throw CourseNotFoundException(courseCode)

        return course.equivalenceGroupId?.let { groupId ->
            courseRepository.findByEquivalenceGroupId(groupId)
        } ?: listOf(course)
    }

    /**
     * 시간표에 동일 대체 과목 중복 검증
     */
    fun validateNoDuplicateEquivalentCourses(courseCodes: List<Long>): Boolean {
        val groupIds = courseRepository.findByCodeIn(courseCodes)
            .mapNotNull { it.equivalenceGroupId }

        return groupIds.size == groupIds.distinct().size
    }
}
```
