-- PT-207: Update chapel target policy (2026) via DB targets
--
-- Goal
-- - Keep API/business logic unchanged (target allow/deny 기반)
-- - Fix chapel recommendation eligibility by adjusting `target` rows
--
-- Policy (요청 사항)
-- - 비전채플: 재수강반(…09) 및 수강불가 성격의 10채플(…10)을 제외하고,
--   2~4학년은 나머지 모든 비전채플을 수강 가능 대상으로 본다.
-- - 재수강반 채플(…09)은 4학년 재수강(미이수자) 성격으로 4학년만 열어둔다.
--
-- Notes
-- - scope_type / student_type 는 코드(ordinal)로 저장됨
--   - scope_type: UNIVERSITY=0
--   - student_type: GENERAL=0

START TRANSACTION;

-- 1) 2~4학년 비전채플 전체 허용 (10채플/09 제외)
--    - 기존 UNIVERSITY scope allow(0) 타겟이 있으면 중복을 피하기 위해 삭제 후 재삽입
DELETE t
FROM target t
JOIN course c ON c.code = t.course_code
WHERE c.category = 'CHAPEL'
  AND c.division = '비전채플'
  AND c.code NOT IN (2150101509, 2150101510)
  AND t.scope_type = 0
  AND t.student_type = 0
  AND t.is_denied = 0;

INSERT INTO target (
  course_code, scope_type, college_id, department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, student_type, is_strict
)
SELECT
  c.code,
  0 AS scope_type,
  NULL AS college_id,
  NULL AS department_id,
  0 AS grade1,
  1 AS grade2,
  1 AS grade3,
  1 AS grade4,
  0 AS grade5,
  0 AS is_denied,
  0 AS student_type,
  0 AS is_strict
FROM course c
WHERE c.category = 'CHAPEL'
  AND c.division = '비전채플'
  AND c.code NOT IN (2150101509, 2150101510);

-- 2) 재수강반 채플(…09): 4학년만 허용
DELETE FROM target
WHERE course_code = 2150101509
  AND student_type = 0
  AND scope_type = 0;

-- allow: grade4 only
INSERT INTO target (
  course_code, scope_type, college_id, department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, student_type, is_strict
) VALUES (
  2150101509, 0, NULL, NULL,
  0, 0, 0, 1, 0,
  0, 0, 0
);

-- deny: block grade1~3
INSERT INTO target (
  course_code, scope_type, college_id, department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, student_type, is_strict
) VALUES (
  2150101509, 0, NULL, NULL,
  1, 1, 1, 0, 0,
  1, 0, 0
);

-- 3) 10채플(…10): 2~4학년 GENERAL은 deny 처리 (추천/조회에서 제외되도록)
DELETE FROM target
WHERE course_code = 2150101510
  AND student_type = 0
  AND scope_type = 0
  AND is_denied = 1;

INSERT INTO target (
  course_code, scope_type, college_id, department_id,
  grade1, grade2, grade3, grade4, grade5,
  is_denied, student_type, is_strict
) VALUES (
  2150101510, 0, NULL, NULL,
  0, 1, 1, 1, 0,
  1, 0, 0
);

COMMIT;
