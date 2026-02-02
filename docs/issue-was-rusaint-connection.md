# 이슈 추천: WAS ↔ rusaint-service 연결 구현

아래 내용을 GitHub 이슈로 등록하고, 구현 투두를 바탕으로 커밋 후 PR 올리면 됩니다.

---

## 이슈 (복사용)

### Title
`[Usaint] WAS ↔ rusaint-service 연동 (two-track academic/graduation 호출)`

### Labels
`usaint`, `rusaint` (또는 팀 라벨)

### Body

#### 영향 범위
- **API**: 유세인트 동기화/갱신 진입점 (sync 또는 refresh)
- **도메인**: `UsaintService`, `RusaintServiceClient` (usaint ↔ rusaint-service 통신)

#### 구현 내용

**AS-IS**
- `RusaintServiceClient`가 `/api/usaint/snapshot` 단일 엔드포인트만 호출
- rusaint-service는 academic / graduation이 분리된 엔드포인트 제공 중

**TO-BE**
- [ ] WAS에서 rusaint-service를 **two-track**으로 호출: `/api/usaint/snapshot/academic` 먼저 호출 → 응답 수신 즉시 비즈니스 로직(저성적 분류 등) 수행, `/api/usaint/snapshot/graduation`은 **0.5초 뒤** 호출 후 두 응답 병합
- [ ] `RusaintServiceClient`: `getAcademicSnapshot`, `getGraduationSnapshot` 추가 및 two-track 오케스트레이션(또는 기존 `syncUsaintData`를 해당 흐름으로 대체)
- [ ] `UsaintService`: academic 응답으로 저성적 분류·DB 조회 수행, graduation 응답 수신 후 병합하여 최종 스냅샷 형태로 보유 (DB 저장·Redis는 본 이슈 범위 제외)
- [ ] WAS → rusaint 요청 시 **내부 JWT** (Authorization 헤더) 사용. 현재는 placeholder 유지, rusaint-service와 계약 맞춤
- [ ] sync/refresh API: `studentId`, `sToken` 수신 후 위 흐름 수행, 클라이언트에는 최소 응답(예: `summary`, `lastSyncedAt` 등)만 반환

#### 특이사항
- DB 저장·pseudonym 발급·Redis는 별도 이슈로 진행. 본 이슈는 **연결 및 two-track 호출·응답 병합·비즈니스 로직 적용**까지만 구현.
- rusaint-service academic/graduation 응답 스키마에 맞춰 Kotlin DTO 정리 필요.

---

## 구현 투두 (커밋/PR용)

아래 순서대로 진행하면 커밋 단위로 나누기 좋습니다.

| # | 투두 | 커밋 제안 메시지 |
|---|------|------------------|
| 1 | rusaint Python 서비스의 academic/graduation 응답 스키마 확인 후, Kotlin 쪽 `RusaintUsaintDataResponse` 및 academic/graduation 전용 DTO 정리 | `fix(usaint): rusaint academic/graduation 응답 DTO 정리` |
| 2 | `RusaintServiceClient`에 `getAcademicSnapshot(studentId, sToken)`, `getGraduationSnapshot(studentId, sToken)` 메서드 추가 (내부 JWT 헤더 포함) | `feat(usaint): RusaintServiceClient에 academic/graduation 단일 호출 메서드 추가` |
| 3 | `RusaintServiceClient`에서 two-track 호출(academic 호출 → 0.5초 후 graduation 호출) 및 응답 병합하여 `RusaintUsaintDataResponse` 반환 로직 구현. 기존 `syncUsaintData`를 이 흐름으로 대체하거나, 새 메서드로 두고 `syncUsaintData`가 이를 호출하도록 변경 | `feat(usaint): rusaint two-track 호출(academic → graduation) 및 응답 병합` |
| 4 | `UsaintService.syncUsaintData`: two-track 결과 수신 후 저성적 분류(`classifyLowGradeSubjectCodes`) 등 기존 비즈니스 로직 적용, 최소 응답 DTO(예: `UsaintSyncResponse`) 반환. DB 저장은 하지 않음 | `feat(usaint): UsaintService two-track 결과 수신 및 비즈니스 로직 적용` |
| 5 | WAS → rusaint 요청 시 Authorization 헤더에 내부 JWT placeholder 유지. (선택) rusaint-service와 계약 맞춰 검증 로직 또는 환경 설정 정리 | `chore(usaint): rusaint 내부 JWT 헤더 placeholder 유지 및 설정 정리` |
| 6 | sync/refresh 엔드포인트(UsaintController)에서 `studentId`, `sToken` 수신 후 `UsaintService.syncUsaintData` 호출, 응답 반환. 기존 엔드포인트가 있으면 해당 흐름으로 연결 | `feat(usaint): sync/refresh API에서 two-track rusaint 연동 호출` |
| 7 | `RusaintServiceException` 등 rusaint 연동 실패 시 예외 처리 및 글로벌 핸들러에서 HTTP 상태/메시지 매핑 | `feat(usaint): rusaint 연동 실패 예외 처리 및 HTTP 응답 매핑` |
| 8 | UsaintService·RusaintServiceClient 단위 테스트 추가, `src/main/resources/http/rusaint.http`에 two-track 연동 예제 추가 | `test(usaint): two-track 연동 단위 테스트 및 rusaint.http 예제` |

### PR 제목 예시
`[Usaint] WAS ↔ rusaint-service two-track 연동 (academic → graduation)`

### PR 설명에 넣을 체크리스트
- [ ] rusaint-service academic/graduation 스키마에 맞춰 DTO 정리
- [ ] RusaintServiceClient two-track 호출 및 병합 구현
- [ ] UsaintService에서 two-track 결과로 저성적 분류 등 비즈니스 로직 적용
- [ ] sync/refresh API 연동
- [ ] RusaintServiceException 처리 및 HTTP 매핑
- [ ] 단위 테스트 및 rusaint.http 예제
