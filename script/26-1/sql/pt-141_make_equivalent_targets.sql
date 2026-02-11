-- PT-141: AI소프트웨어학부/소프트웨어학부/AI융합학부 target을 필요충분(동치) 관계로 정렬
-- 전략: 3개 학과 target의 합집합(union signature)을 각 학과에 모두 채운다.

START TRANSACTION;

-- 공통 union signature를 임시 테이블에 적재
DROP TEMPORARY TABLE IF EXISTS tmp_pt141_union;
CREATE TEMPORARY TABLE tmp_pt141_union AS
SELECT DISTINCT
  course_code,
  scope_type,
  student_type,
  college_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, is_strict
FROM target
WHERE department_id IN (2, 5, 9);

-- union -> AI소프트웨어학부(9)
INSERT INTO target (
  college_id,
  course_code,
  department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, is_strict,
  scope_type, student_type
)
SELECT
  u.college_id,
  u.course_code,
  9 AS department_id,
  u.grade1, u.grade2, u.grade3, u.grade4, u.grade5,
  u.is_denied, u.is_strict,
  u.scope_type, u.student_type
FROM tmp_pt141_union u
WHERE NOT EXISTS (
  SELECT 1
  FROM target t
  WHERE t.department_id = 9
    AND t.course_code = u.course_code
    AND t.scope_type = u.scope_type
    AND t.student_type = u.student_type
    AND IFNULL(t.college_id, -1) = IFNULL(u.college_id, -1)
    AND t.grade1 = u.grade1
    AND t.grade2 = u.grade2
    AND t.grade3 = u.grade3
    AND t.grade4 = u.grade4
    AND t.grade5 = u.grade5
    AND t.is_denied = u.is_denied
    AND t.is_strict = u.is_strict
);

-- union -> 소프트웨어학부(5)
INSERT INTO target (
  college_id,
  course_code,
  department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, is_strict,
  scope_type, student_type
)
SELECT
  u.college_id,
  u.course_code,
  5 AS department_id,
  u.grade1, u.grade2, u.grade3, u.grade4, u.grade5,
  u.is_denied, u.is_strict,
  u.scope_type, u.student_type
FROM tmp_pt141_union u
WHERE NOT EXISTS (
  SELECT 1
  FROM target t
  WHERE t.department_id = 5
    AND t.course_code = u.course_code
    AND t.scope_type = u.scope_type
    AND t.student_type = u.student_type
    AND IFNULL(t.college_id, -1) = IFNULL(u.college_id, -1)
    AND t.grade1 = u.grade1
    AND t.grade2 = u.grade2
    AND t.grade3 = u.grade3
    AND t.grade4 = u.grade4
    AND t.grade5 = u.grade5
    AND t.is_denied = u.is_denied
    AND t.is_strict = u.is_strict
);

-- union -> AI융합학부(2)
INSERT INTO target (
  college_id,
  course_code,
  department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, is_strict,
  scope_type, student_type
)
SELECT
  u.college_id,
  u.course_code,
  2 AS department_id,
  u.grade1, u.grade2, u.grade3, u.grade4, u.grade5,
  u.is_denied, u.is_strict,
  u.scope_type, u.student_type
FROM tmp_pt141_union u
WHERE NOT EXISTS (
  SELECT 1
  FROM target t
  WHERE t.department_id = 2
    AND t.course_code = u.course_code
    AND t.scope_type = u.scope_type
    AND t.student_type = u.student_type
    AND IFNULL(t.college_id, -1) = IFNULL(u.college_id, -1)
    AND t.grade1 = u.grade1
    AND t.grade2 = u.grade2
    AND t.grade3 = u.grade3
    AND t.grade4 = u.grade4
    AND t.grade5 = u.grade5
    AND t.is_denied = u.is_denied
    AND t.is_strict = u.is_strict
);

COMMIT;
