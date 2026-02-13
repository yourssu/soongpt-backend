-- PT-160 verify: AI소프트웨어학부 타전공인정(CROSS_MAJOR/RECOGNIZED) 분류가 소프트웨어학부와 동일한지 검증

-- 1) count 비교
SELECT d.name, COUNT(DISTINCT csmc.course_code) AS cnt
FROM department d
LEFT JOIN course_secondary_major_classification csmc
  ON csmc.department_id=d.id
  AND csmc.track_type='CROSS_MAJOR'
  AND csmc.completion_type='RECOGNIZED'
WHERE d.name IN ('소프트웨어학부','AI소프트웨어학부')
GROUP BY d.id,d.name
ORDER BY d.name;

-- 2) set difference (sw \ ai)
SELECT s.course_code
FROM course_secondary_major_classification s
JOIN department sw ON sw.id=s.department_id AND sw.name='소프트웨어학부'
LEFT JOIN course_secondary_major_classification a
  ON a.course_code=s.course_code
  AND a.track_type=s.track_type
  AND a.completion_type=s.completion_type
  AND a.department_id=(SELECT id FROM department WHERE name='AI소프트웨어학부')
WHERE s.track_type='CROSS_MAJOR'
  AND s.completion_type='RECOGNIZED'
  AND a.id IS NULL
ORDER BY s.course_code;

-- 3) set difference (ai \ sw)
SELECT a.course_code
FROM course_secondary_major_classification a
JOIN department ai ON ai.id=a.department_id AND ai.name='AI소프트웨어학부'
LEFT JOIN course_secondary_major_classification s
  ON s.course_code=a.course_code
  AND s.track_type=a.track_type
  AND s.completion_type=a.completion_type
  AND s.department_id=(SELECT id FROM department WHERE name='소프트웨어학부')
WHERE a.track_type='CROSS_MAJOR'
  AND a.completion_type='RECOGNIZED'
  AND s.id IS NULL
ORDER BY a.course_code;
