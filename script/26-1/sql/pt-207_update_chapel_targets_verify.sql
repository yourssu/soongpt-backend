-- PT-207 verify

-- 01~09 분반: UNIVERSITY scope GENERAL allow가 (grade2~4)로 존재하는지
SELECT
  course_code,
  COUNT(*) AS university_allows
FROM target
WHERE course_code BETWEEN 2150101501 AND 2150101509
  AND scope_type = 0
  AND student_type = 0
  AND is_denied = 0
  AND grade1 = 0
  AND grade2 = 1
  AND grade3 = 1
  AND grade4 = 1
  AND grade5 = 0
GROUP BY course_code
ORDER BY course_code;

-- 10채플(…10): 현재 target 정책 확인 (변경 없음)
SELECT *
FROM target
WHERE course_code = 2150101510
ORDER BY is_denied ASC, student_type ASC, scope_type ASC;
