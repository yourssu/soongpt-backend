-- PT-141 검증 SQL

-- 학과별 target row 수
SELECT d.name, COUNT(*) AS target_rows
FROM target t
JOIN department d ON d.id = t.department_id
WHERE t.department_id IN (9, 5, 2)
GROUP BY d.name
ORDER BY d.name;

-- AI소프트웨어학부 rows 기준으로 소프트웨어/AI융합에 누락된 row 수
SELECT
  '소프트웨어학부(5)' AS target_dept,
  COUNT(*) AS missing_rows
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
  )
UNION ALL
SELECT
  'AI융합학부(2)' AS target_dept,
  COUNT(*) AS missing_rows
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
