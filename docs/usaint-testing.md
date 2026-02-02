# 유세인트(rusaint) 연동 테스트 방법

## 바로 테스트하기 (요약)

1. **학번·sToken 설정**
   `src/main/resources/http/http-client.private.env.json.example` 를 복사해
   `http-client.private.env.json` 으로 만들고, **실제 학번**과 **유세인트 sToken**을 넣습니다.
2. **rusaint-service 실행**
   `rusaint_service/.env` 에 시크릿 설정 후, **어디서 실행해도** `rusaint_service/` 안의 `.env` 가 로드됩니다.
   ```bash
   cd rusaint_service
   uvicorn app.main:app --reload --port 8000
   ```
   (프로젝트 루트에서 `uvicorn rusaint_service.app.main:app ...` 처럼 켜도 `.env` 는 `rusaint_service/.env` 기준으로 읽습니다.)
3. **WAS 실행**
   `./gradlew bootRun --args='--spring.profiles.active=local'`
   (로컬은 rusaint 기본 URL·시크릿이 yml에 있어서 env 없이 실행 가능)
4. **HTTP로 호출**
   `src/main/resources/http/rusaint.http` 에서
   `POST http://localhost:8080/api/usaint/sync` 요청을 Run(▶) 합니다.
   - **반드시** 요청 실행 시 환경(Environment)을 **`local`** 로 선택해야 `studentId`, `sToken`이 치환됩니다.
   - env 파일: `src/main/resources/http/http-client.private.env.json` (rusaint.http와 같은 폴더)

---

## 1. 실제 값이 들어가는 곳

| 용도 | 설정 위치 | 넣어야 할 값 |
|------|-----------|--------------|
| **HTTP 파일에서 요청** (학번·sToken) | `src/main/resources/http/http-client.private.env.json` | `studentId`, `sToken` |
| **WAS → rusaint 호출** (JWT·pseudonym·연결) | WAS 실행 시 환경 변수 | `RUSAINT_BASE_URL`, `RUSAINT_INTERNAL_JWT_SECRET`, `RUSAINT_PSEUDONYM_SECRET` |
| **rusaint-service** (JWT 검증·pseudonym) | `rusaint_service/.env` | `INTERNAL_JWT_SECRET`, `PSEUDONYM_SECRET` (WAS와 **동일 값** 사용) |

---

## 2. HTTP로 테스트할 때 (rusaint.http)

### 2-1. 학번·sToken 설정

1. **`http-client.private.env.json` 만들기**
   예시 파일을 복사해서 같은 디렉터리에 `http-client.private.env.json`을 만듭니다.

   ```bash
   cp src/main/resources/http/http-client.private.env.json.example \
      src/main/resources/http/http-client.private.env.json
   ```

2. **실제 값 넣기**
   `http-client.private.env.json`을 열어서 **실제 학번**과 **유세인트 sToken**을 넣습니다.

   - **학번**: 본인 학번 (예: `20233009`)
   - **sToken**: 유세인트 로그인 후 발급되는 SSO 토큰 (브라우저 개발자도구 → Application → Cookies 등에서 확인하거나, 로그인 응답에서 복사)

   ```json
   {
     "local": {
       "studentId": "20233009",
       "sToken": "실제_sToken_문자열",
       "internalJwt": ""
     }
   }
   ```

3. **IDE에서 HTTP 실행**
   - `src/main/resources/http/rusaint.http` 열기
   - **WAS 경유 테스트**: `POST http://localhost:8080/api/usaint/sync` 블록에서 Run (▶)
     → 이때는 `studentId`, `sToken`만 쓰이고, `internalJwt`는 비워두어도 됩니다 (WAS가 알아서 JWT 발급해서 rusaint 호출).

   - **Python rusaint 직접 호출**할 때만 `internalJwt`가 필요합니다.
     → 개발 모드(DEBUG=true)면 rusaint-service가 `internal-jwt-placeholder`를 허용하므로, 그때는 `"internalJwt": "internal-jwt-placeholder"` 로 두면 됩니다.

---

## 3. WAS / rusaint-service 환경 변수 (로컬)

### 3-1. rusaint-service (Python)

`rusaint_service/.env` (또는 `env.example` → `.env`):

```bash
# WAS와 동일한 값 사용 (최소 32자 권장)
INTERNAL_JWT_SECRET=동일한-시크릿-문자열
PSEUDONYM_SECRET=동일한-pseudonym-시크릿

# 개발 시 placeholder JWT 허용
DEBUG=true
```

**로컬 기본값으로 빠르게 테스트하려면**
WAS `application-local.yml` 기본값과 맞춰서 rusaint `.env`에 다음만 넣어도 됩니다.

```bash
INTERNAL_JWT_SECRET=local-internal-jwt-secret-min-32-chars
PSEUDONYM_SECRET=local-pseudonym-secret-min-32-chars
DEBUG=true
```

### 3-2. WAS (Kotlin)

**로컬(local 프로필)** 에서는 `application-local.yml`에 이미 기본값이 있어서, **환경 변수를 안 넣어도** 동작합니다.

- `base-url`: `http://localhost:8000`
- `internal-jwt-secret`: `local-internal-jwt-secret-min-32-chars`
- `pseudonym-secret`: `local-pseudonym-secret-min-32-chars`

다른 주소/시크릿을 쓰고 싶을 때만 환경 변수로 덮어씁니다.

**주의:** WAS 쪽 env 이름은 **반드시** 아래와 같아야 합니다. `INTERNAL_JWT_SECRET`처럼 `RUSAINT_` 접두어 없이 넣으면 Spring이 읽지 못해 기본값으로 동작하고, rusaint와 시크릿이 달라져 **401 InvalidSignatureError**가 납니다.

```bash
export RUSAINT_BASE_URL=http://localhost:8000
export RUSAINT_INTERNAL_JWT_SECRET=여기에_시크릿_문자열
export RUSAINT_PSEUDONYM_SECRET=여기에_시크릿_문자열
```

---

## 4. 테스트 순서 (전체 플로우)

1. **rusaint-service 실행**

   ```bash
   cd rusaint_service
   cp env.example .env   # 최초 1회, INTERNAL_JWT_SECRET 등 위 3-1처럼 설정
   pip install -r requirements.txt
   uvicorn app.main:app --reload --port 8000
   ```

2. **WAS 실행**

   ```bash
   # 터미널에서
   export RUSAINT_BASE_URL=http://localhost:8000
   export RUSAINT_INTERNAL_JWT_SECRET=my-internal-jwt-secret-at-least-32-chars
   export RUSAINT_PSEUDONYM_SECRET=my-pseudonym-secret-at-least-32-chars

   ./gradlew bootRun --args='--spring.profiles.active=local'
   ```

   또는 IDE Run Configuration에 위 환경 변수 설정 후 `bootRun` (profile: local).

3. **HTTP로 호출**

   - `rusaint.http`에서 **첫 번째 요청**
     `POST http://localhost:8080/api/usaint/sync`
     실행 시, `http-client.private.env.json`의 `studentId`, `sToken`이 자동으로 들어갑니다.
   - 응답으로 `{"summary":"usaint data synced"}` 등이 오면, WAS → rusaint two-track 호출 및 JWT·pseudonym 연동이 동작하는 것입니다.

---

## 5. 503 vs 401 (응답 상태 코드)

- **WAS가 503을 반환**하는 이유: rusaint-service 호출이 실패했을 때, WAS는 “의존 서비스 장애”로 **503 Service Unavailable**을 클라이언트에 돌려줍니다.
- **message에 401이 나오는 이유**: rusaint가 **401 Unauthorized**를 돌려줬다는 뜻입니다. 즉, “WAS가 503을 준 이유 = rusaint가 401을 줬기 때문”이고, message의 `status=401`이 그 하위 결과를 알려주는 겁니다.
- **정리**: `status: 503` = WAS 응답 코드, `message` 안의 `status=401` = rusaint 응답 코드. 401이 나오면 대부분 **JWT 시크릿 불일치**입니다 (아래 6번 참고).

---

## 6. 시크릿 vs 토큰 (JWT 401 / InvalidSignatureError 해결)

**혼동하기 쉬운 부분:**

| 구분 | 의미 | 어디에 넣는지 |
|------|------|----------------|
| **JWT 시크릿** | 서명/검증에 쓰는 **비밀 키** (문자열 하나) | WAS `application-local.yml`의 `rusaint.internal-jwt-secret`, rusaint `.env`의 `INTERNAL_JWT_SECRET` |
| **JWT 토큰** | 그 시크릿으로 **만들어진 토큰 문자열** (Bearer 뒤에 붙이는 값) | `http-client.private.env.json`의 `internalJwt` (Python을 **직접** 호출할 때만 사용) |

- **WAS → rusaint 호출** 시: WAS가 **시크릿**으로 JWT를 만들어서 Authorization 헤더에 넣고, rusaint는 **같은 시크릿**으로 검증합니다.
- 따라서 **rusaint의 `INTERNAL_JWT_SECRET`** 은 **WAS의 `rusaint.internal-jwt-secret`(또는 env `RUSAINT_INTERNAL_JWT_SECRET`)과 완전히 같은 문자열**이어야 합니다.
- **`http-client.private.env.json`의 `internalJwt`** 는 “직접 Python에 요청 보낼 때 쓰는 **토큰 값**”일 뿐, 시크릿이 아닙니다.
  이 값을 rusaint의 `INTERNAL_JWT_SECRET`에 넣으면 **InvalidSignatureError / 401** 이 납니다.
  rusaint `.env`에는 반드시 **WAS와 동일한 시크릿 문자열**을 넣어야 합니다 (예: `local-internal-jwt-secret-min-32-chars`).

---

## 7. PSEUDONYM_SECRET / 필요한 환경 변수 정리

### PSEUDONYM_SECRET

- **발급하는 값이 아닙니다.**
  WAS와 rusaint가 **같이 쓰기로 정한 임의의 문자열**을 정해서, 둘 다 같은 값으로 설정하면 됩니다.
- **권장**: 32자 이상의 랜덤 문자열.
  로컬에서는 `application-local.yml` 기본값 `local-pseudonym-secret-min-32-chars`를 쓰면 되고, rusaint `.env`에도 **같은 값**을 넣으면 됩니다.

### 로컬에서 필요한 환경 변수 요약

**WAS (로컬):**

- `application-local.yml`에 기본값이 있으므로 **별도 env 없이** 실행해도 됩니다.
- 필요하면 덮어쓰기: `RUSAINT_BASE_URL`, `RUSAINT_INTERNAL_JWT_SECRET`, `RUSAINT_PSEUDONYM_SECRET`

**rusaint-service (로컬):**

- **필수**: `INTERNAL_JWT_SECRET` = WAS의 `rusaint.internal-jwt-secret`과 **동일**
- **필수**: `PSEUDONYM_SECRET` = WAS의 `rusaint.pseudonym-secret`과 **동일**
- **선택**: `DEBUG=true` (개발 시 placeholder JWT 허용용)

**http-client.private.env.json (HTTP 파일용):**

- **필수**: `studentId`, `sToken` (실제 학번·유세인트 sToken)
- **선택**: `internalJwt` (Python을 **직접** 호출할 때만; WAS 경유 시에는 비워두면 됨)

---

## 8. 요약

- **실제 값**은 반드시 다음 두 곳에 넣어야 합니다.
  - **HTTP 테스트**: `http-client.private.env.json` → `studentId`, `sToken`
  - **서버 연동**: rusaint `.env` → `INTERNAL_JWT_SECRET`, `PSEUDONYM_SECRET` (WAS와 **동일**)
- WAS 로컬은 yml 기본값 때문에 **env 없이** 실행 가능. rusaint만 `.env`에 **WAS와 같은 시크릿** 두면 됩니다.
- **401 / InvalidSignatureError** 나오면: rusaint의 `INTERNAL_JWT_SECRET`이 **WAS의 internal-jwt-secret과 같은지** 확인하고, `http-client.private.env.json`의 `internalJwt`(토큰)를 시크릿 자리에 넣지 않았는지 확인하면 됩니다.
- `PSEUDONYM_SECRET`은 “발급”이 아니라, WAS·rusaint가 **같은 문자열**로 맞춰두면 됩니다.
