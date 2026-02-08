# SSO 콜백 (GET /api/sso/callback)

## 개요

- **목적**: 숭실대 SSO 로그인 완료 후 리다이렉트를 수신하여 인증 처리를 수행한다.
- **흐름**:
  1. sToken 형식 및 유효성 검증 (rusaint-service 동기 호출, 약 1-2초)
  2. pseudonym 생성 (HMAC-SHA256 기반)
  3. JWT 쿠키(`soongpt_auth`) 발급
  4. 비동기로 rusaint 데이터 동기화 시작
  5. 프론트엔드 `/loading` 페이지로 302 리다이렉트
- **후속**: 프론트엔드는 [동기화 상태 조회 (GET /api/sync/status)](sync_status.md)를 폴링하여 완료를 확인한다.

---

## Request

### Query Parameters

| Name     | Type   | Required | Description                |
|----------|--------|----------|----------------------------|
| `sToken` | string | Yes      | 숭실대 SSO 인증 토큰 (Base64, 100~1000자) |
| `sIdno`  | string | Yes      | 학번 (`20150000`~`20299999` 형식) |

### 예시

```
GET /api/sso/callback?sToken={sToken}&sIdno={studentId}
```

---

## Response

### 성공 (302 Found)

sToken 검증 성공 시, JWT 쿠키를 발급하고 프론트엔드로 리다이렉트한다.

- **Status**: `302 Found`
- **Location**: `{frontendUrl}/loading`
- **Set-Cookie**: `soongpt_auth={JWT}; Path=/; HttpOnly; SameSite=Lax`

### 실패 — sToken 형식 오류 (302 Found)

- **Status**: `302 Found`
- **Location**: `{frontendUrl}/error?reason=invalid_token`
- **Set-Cookie**: 없음

### 실패 — sToken 만료/무효 (302 Found)

rusaint-service의 토큰 검증에서 실패한 경우 (만료 등).

- **Status**: `302 Found`
- **Location**: `{frontendUrl}/error?reason=token_expired`
- **Set-Cookie**: 없음

---

## 인증 흐름 다이어그램

```
브라우저 → SSO 로그인 → 숭실대 SSO
    ↓
GET /api/sso/callback?sToken=...&sIdno=...
    ↓
WAS: sToken 검증 (동기) → 실패 시 /error로 리다이렉트
    ↓ 성공
WAS: pseudonym 생성, JWT 쿠키 발급, 비동기 rusaint fetch 시작
    ↓
302 → {frontendUrl}/loading (쿠키 포함)
    ↓
프론트: GET /api/sync/status 폴링 (쿠키 자동 전송)
```
