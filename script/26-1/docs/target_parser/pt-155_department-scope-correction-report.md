# PT-155 실행 결과 보고서: UNIVERSITY -> DEPARTMENT 수강대상 범위 보정

## 실행 개요
- 대상 과목: 15개
- 목적: `target.scope_type=UNIVERSITY(0)`의 GENERAL allow row를
  개설학과 기준 `scope_type=DEPARTMENT(2)` allow row로 전환

## 실행 정보
- 백업 테이블: `target_backup_pt155_20260212_2234`
- 실행 일시: 2026-02-12

## 집계 결과
- 변환 전 UNIVERSITY allow row: **28**
- 변환 전 DEPARTMENT allow row: **0**
- 삽입된 DEPARTMENT allow row: **28**
- 삭제된 UNIVERSITY allow row: **28**
- 변환 후 UNIVERSITY allow row: **0**
- 변환 후 DEPARTMENT allow row: **28**
- deny row 변화: **1 -> 1 (변화 없음)**

## 과목별 확인 요약
- 15개 과목 모두 `UNIVERSITY allow=0`, `DEPARTMENT allow>0` 상태로 전환 완료
- `2150465601(PR론)`의 deny row 1건 유지 확인

## 산출물
- 실행 SQL: `script/26-1/sql/pt-155_department_scope_correction.sql`
- 검증 SQL: `script/26-1/sql/pt-155_department_scope_correction_verify.sql`
- 계획 문서: `script/26-1/docs/target_parser/pt-155_department-scope-correction-plan.md`
