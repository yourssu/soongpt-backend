-- PT-155: 대상 15개 과목의 UNIVERSITY allow row를 DEPARTMENT allow row로 보정
-- 대상: MAJOR_BASIC/MAJOR_REQUIRED/MAJOR_ELECTIVE 과목 중 지정 코드 15개

START TRANSACTION;

-- 1) 변환 대상(UNIVERSITY allow + GENERAL) 임시 집합
DROP TEMPORARY TABLE IF EXISTS tmp_pt155_source;
CREATE TEMPORARY TABLE tmp_pt155_source AS
SELECT
  t.id AS source_target_id,
  t.course_code,
  c.department AS course_department_name,
  d.id AS mapped_department_id,
  t.grade1, t.grade2, t.grade3, t.grade4, t.grade5,
  t.student_type,
  t.is_strict,
  t.is_denied
FROM target t
JOIN course c ON c.code = t.course_code
JOIN department d ON d.name = c.department
WHERE t.course_code IN (
  2150078601,2150083201,2150084001,2150084002,2150084003,
  2150084501,2150084601,2150085901,2150128501,2150128601,
  2150129501,2150155001,2150156401,2150386604,2150465601
)
  AND t.scope_type = 0
  AND t.student_type = 0
  AND t.is_denied = 0;

-- 2) DEPARTMENT row 삽입 (중복 방지)
INSERT INTO target (
  course_code,
  scope_type,
  college_id,
  department_id,
  grade1, grade2, grade3, grade4, grade5,
  student_type,
  is_strict,
  is_denied
)
SELECT
  s.course_code,
  2 AS scope_type,
  NULL AS college_id,
  s.mapped_department_id,
  s.grade1, s.grade2, s.grade3, s.grade4, s.grade5,
  s.student_type,
  s.is_strict,
  s.is_denied
FROM tmp_pt155_source s
WHERE NOT EXISTS (
  SELECT 1 FROM target t
  WHERE t.course_code = s.course_code
    AND t.scope_type = 2
    AND IFNULL(t.department_id, -1) = IFNULL(s.mapped_department_id, -1)
    AND t.student_type = s.student_type
    AND t.is_denied = s.is_denied
    AND t.is_strict = s.is_strict
    AND t.grade1 = s.grade1
    AND t.grade2 = s.grade2
    AND t.grade3 = s.grade3
    AND t.grade4 = s.grade4
    AND t.grade5 = s.grade5
);

-- 3) 기존 UNIVERSITY allow row 삭제
DELETE t
FROM target t
JOIN tmp_pt155_source s ON s.source_target_id = t.id;

COMMIT;
