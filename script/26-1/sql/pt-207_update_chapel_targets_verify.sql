-- PT-207 verify

-- 비전채플(09/10 제외): UNIVERSITY scope allow가 (grade2~4)로 존재하는지
SELECT
  COUNT(*) AS university_allows
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category = 'CHAPEL'
  AND c.division = '비전채플'
  AND c.code NOT IN (2150101509, 2150101510)
  AND t.scope_type = 0
  AND t.student_type = 0
  AND t.is_denied = 0
  AND t.grade1 = 0
  AND t.grade2 = 1
  AND t.grade3 = 1
  AND t.grade4 = 1
  AND t.grade5 = 0;

-- 09 재수강반: allow grade4 only, deny grade1~3
SELECT *
FROM target
WHERE course_code = 2150101509
  AND scope_type = 0
  AND student_type = 0
ORDER BY is_denied ASC;

-- 10채플: deny grade2~4
SELECT *
FROM target
WHERE course_code = 2150101510
  AND scope_type = 0
  AND student_type = 0
  AND is_denied = 1;
