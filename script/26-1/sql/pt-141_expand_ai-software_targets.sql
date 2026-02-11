-- PT-141: AI소프트웨어학부 대상 과목을 소프트웨어학부/AI융합학부에도 동일 조건으로 확장
-- 기준 학과: AI소프트웨어학부(department_id=9)
-- 확장 학과: 소프트웨어학부(department_id=5), AI융합학부(department_id=2)

START TRANSACTION;

-- 1) AI소프트웨어학부 -> 소프트웨어학부
INSERT INTO target (
  college_id,
  course_code,
  department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, is_strict,
  scope_type, student_type
)
SELECT
  s.college_id,
  s.course_code,
  5 AS department_id,
  s.grade1, s.grade2, s.grade3, s.grade4, s.grade5,
  s.is_denied, s.is_strict,
  s.scope_type, s.student_type
FROM target s
WHERE s.department_id = 9
  AND NOT EXISTS (
    SELECT 1
    FROM target t
    WHERE t.course_code = s.course_code
      AND t.scope_type = s.scope_type
      AND t.student_type = s.student_type
      AND IFNULL(t.college_id, -1) = IFNULL(s.college_id, -1)
      AND IFNULL(t.department_id, -1) = 5
      AND t.grade1 = s.grade1
      AND t.grade2 = s.grade2
      AND t.grade3 = s.grade3
      AND t.grade4 = s.grade4
      AND t.grade5 = s.grade5
      AND t.is_denied = s.is_denied
      AND t.is_strict = s.is_strict
  );

-- 2) AI소프트웨어학부 -> AI융합학부
INSERT INTO target (
  college_id,
  course_code,
  department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, is_strict,
  scope_type, student_type
)
SELECT
  s.college_id,
  s.course_code,
  2 AS department_id,
  s.grade1, s.grade2, s.grade3, s.grade4, s.grade5,
  s.is_denied, s.is_strict,
  s.scope_type, s.student_type
FROM target s
WHERE s.department_id = 9
  AND NOT EXISTS (
    SELECT 1
    FROM target t
    WHERE t.course_code = s.course_code
      AND t.scope_type = s.scope_type
      AND t.student_type = s.student_type
      AND IFNULL(t.college_id, -1) = IFNULL(s.college_id, -1)
      AND IFNULL(t.department_id, -1) = 2
      AND t.grade1 = s.grade1
      AND t.grade2 = s.grade2
      AND t.grade3 = s.grade3
      AND t.grade4 = s.grade4
      AND t.grade5 = s.grade5
      AND t.is_denied = s.is_denied
      AND t.is_strict = s.is_strict
  );

COMMIT;
