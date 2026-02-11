-- PT-141 동치화 검증

-- 1) 학과별 target row 수
SELECT department_id, COUNT(*) AS row_count
FROM target
WHERE department_id IN (2,5,9)
GROUP BY department_id
ORDER BY department_id;

-- 2) pairwise row-level 차이(0이어야 동치)
-- 9 \ 5
SELECT COUNT(*) AS a9_not_5
FROM target a
WHERE a.department_id=9
  AND NOT EXISTS (
    SELECT 1 FROM target b
    WHERE b.department_id=5
      AND b.course_code=a.course_code
      AND b.scope_type=a.scope_type
      AND b.student_type=a.student_type
      AND IFNULL(b.college_id,-1)=IFNULL(a.college_id,-1)
      AND b.grade1=a.grade1 AND b.grade2=a.grade2 AND b.grade3=a.grade3 AND b.grade4=a.grade4 AND b.grade5=a.grade5
      AND b.is_denied=a.is_denied AND b.is_strict=a.is_strict
  );

-- 5 \ 9
SELECT COUNT(*) AS a5_not_9
FROM target a
WHERE a.department_id=5
  AND NOT EXISTS (
    SELECT 1 FROM target b
    WHERE b.department_id=9
      AND b.course_code=a.course_code
      AND b.scope_type=a.scope_type
      AND b.student_type=a.student_type
      AND IFNULL(b.college_id,-1)=IFNULL(a.college_id,-1)
      AND b.grade1=a.grade1 AND b.grade2=a.grade2 AND b.grade3=a.grade3 AND b.grade4=a.grade4 AND b.grade5=a.grade5
      AND b.is_denied=a.is_denied AND b.is_strict=a.is_strict
  );

-- 9 \ 2
SELECT COUNT(*) AS a9_not_2
FROM target a
WHERE a.department_id=9
  AND NOT EXISTS (
    SELECT 1 FROM target b
    WHERE b.department_id=2
      AND b.course_code=a.course_code
      AND b.scope_type=a.scope_type
      AND b.student_type=a.student_type
      AND IFNULL(b.college_id,-1)=IFNULL(a.college_id,-1)
      AND b.grade1=a.grade1 AND b.grade2=a.grade2 AND b.grade3=a.grade3 AND b.grade4=a.grade4 AND b.grade5=a.grade5
      AND b.is_denied=a.is_denied AND b.is_strict=a.is_strict
  );

-- 2 \ 9
SELECT COUNT(*) AS a2_not_9
FROM target a
WHERE a.department_id=2
  AND NOT EXISTS (
    SELECT 1 FROM target b
    WHERE b.department_id=9
      AND b.course_code=a.course_code
      AND b.scope_type=a.scope_type
      AND b.student_type=a.student_type
      AND IFNULL(b.college_id,-1)=IFNULL(a.college_id,-1)
      AND b.grade1=a.grade1 AND b.grade2=a.grade2 AND b.grade3=a.grade3 AND b.grade4=a.grade4 AND b.grade5=a.grade5
      AND b.is_denied=a.is_denied AND b.is_strict=a.is_strict
  );
