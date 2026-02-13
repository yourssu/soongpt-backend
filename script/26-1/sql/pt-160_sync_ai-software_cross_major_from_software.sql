-- PT-160: AI소프트웨어학부 타전공인정(CROSS_MAJOR/RECOGNIZED) 분류를 소프트웨어학부와 동일하게 맞춤
--
-- 배경
-- - course_secondary_major_classification 에서 AI소프트웨어학부(9)의 CROSS_MAJOR/RECOGNIZED가 비어있어
--   /api/courses/by-track?trackType=CROSS_MAJOR 조회가 빈 목록으로 내려옴
-- - 정책적으로 AI소프트웨어학부의 타전공인정 과목 목록은 소프트웨어학부와 동일해야 함
--
-- 실행 전 확인
-- SELECT d.name, COUNT(DISTINCT csmc.course_code) AS cnt
-- FROM department d
-- LEFT JOIN course_secondary_major_classification csmc
--   ON csmc.department_id=d.id AND csmc.track_type='CROSS_MAJOR' AND csmc.completion_type='RECOGNIZED'
-- WHERE d.name IN ('소프트웨어학부','AI소프트웨어학부')
-- GROUP BY d.id,d.name;
--
-- 주의
-- - idempotent: NOT EXISTS 조건으로 중복 삽입 방지
-- - 원본(raw_classification/raw_department_token)은 소프트웨어학부 값을 그대로 복사

INSERT INTO course_secondary_major_classification
    (course_code, track_type, completion_type, department_id, raw_classification, raw_department_token)
SELECT
    s.course_code,
    s.track_type,
    s.completion_type,
    ai.id AS department_id,
    s.raw_classification,
    s.raw_department_token
FROM course_secondary_major_classification s
JOIN department sw ON sw.id = s.department_id AND sw.name = '소프트웨어학부'
JOIN department ai ON ai.name = 'AI소프트웨어학부'
WHERE s.track_type = 'CROSS_MAJOR'
  AND s.completion_type = 'RECOGNIZED'
  AND NOT EXISTS (
      SELECT 1
      FROM course_secondary_major_classification t
      WHERE t.department_id = ai.id
        AND t.track_type = s.track_type
        AND t.completion_type = s.completion_type
        AND t.course_code = s.course_code
  );
