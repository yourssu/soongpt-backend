# Rusaint 연동 구현 상태

## ✅ 이미 구현된 것

| 항목 | 상태 | 위치 |
|------|------|------|
| WAS two-track 호출 | ✅ | RusaintServiceClient: academic → 0.5초 → graduation → 병합 |
| getAcademicSnapshot / getGraduationSnapshot | ✅ | RusaintServiceClient |
| syncUsaintData two-track 오케스트레이션 | ✅ | RusaintServiceClient.syncUsaintData |
| UsaintService: academic 결과로 저성적 분류·DB 조회 | ✅ | classifyLowGradeSubjectCodes(courseRepository.groupByCategory) |
| graduation 수신 후 병합하여 최종 스냅샷 보유 | ✅ | RusaintServiceClient에서 병합 후 UsaintService에 RusaintUsaintDataResponse 전달 |
| WAS → rusaint 요청 시 Authorization 헤더 | ✅ | buildRequestEntity에서 setBearerAuth(createInternalJwt()) |

## ⏳ 미구현 (이번에 구현)

1. **실제 내부 JWT 생성** – createInternalJwt()가 placeholder 반환 중 → 시크릿 기반 HS256 JWT 발급
2. **rusaint_service에서 pseudonym 생성 후 클라이언트(WAS)에게 반환** – Python 응답에 pseudonym 필드 추가
3. **DB에 스냅샷 저장** – pseudonym 기준 usaint_snapshot 테이블 저장
