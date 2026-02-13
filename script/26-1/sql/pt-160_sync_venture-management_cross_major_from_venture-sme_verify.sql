-- PT-160 verify: 벤처경영학과 타전공인정(CROSS_MAJOR/RECOGNIZED) 분류가 벤처중소기업학과와 동일한지 검증

-- 1) count 비교
SELECT d.name, COUNT(DISTINCT csmc.course_code) AS cnt
FROM department d
LEFT JOIN course_secondary_major_classification csmc
  ON csmc.department_id=d.id
  AND csmc.track_type='CROSS_MAJOR'
  AND csmc.completion_type='RECOGNIZED'
WHERE d.name IN ('벤처중소기업학과','벤처경영학과')
GROUP BY d.id,d.name
ORDER BY d.name;

-- 2) set difference (벤처중소기업학과 \ 벤처경영학과)
SELECT s.course_code
FROM course_secondary_major_classification s
JOIN department src ON src.id=s.department_id AND src.name='벤처중소기업학과'
LEFT JOIN course_secondary_major_classification t
  ON t.course_code=s.course_code
  AND t.track_type=s.track_type
  AND t.completion_type=s.completion_type
  AND t.department_id=(SELECT id FROM department WHERE name='벤처경영학과')
WHERE s.track_type='CROSS_MAJOR'
  AND s.completion_type='RECOGNIZED'
  AND t.id IS NULL
ORDER BY s.course_code;

-- 3) set difference (벤처경영학과 \ 벤처중소기업학과)
SELECT t.course_code
FROM course_secondary_major_classification t
JOIN department vm ON vm.id=t.department_id AND vm.name='벤처경영학과'
LEFT JOIN course_secondary_major_classification s
  ON s.course_code=t.course_code
  AND s.track_type=t.track_type
  AND s.completion_type=t.completion_type
  AND s.department_id=(SELECT id FROM department WHERE name='벤처중소기업학과')
WHERE t.track_type='CROSS_MAJOR'
  AND t.completion_type='RECOGNIZED'
  AND s.id IS NULL
ORDER BY t.course_code;
