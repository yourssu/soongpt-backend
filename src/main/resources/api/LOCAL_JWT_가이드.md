# 로컬 Swagger에서 JWT 발급 가이드 (/api/sso/callback)

팀원이 로컬에서 Swagger UI를 통해 **soongpt_auth JWT**를 발급받는 방법입니다.

---

## 사전 요약

1. **rusaint_service** (Python): 포트 **8001**에서 실행 — SSO sToken 검증용
2. **soongpt-backend** (Kotlin): 포트 **8080** (기본) — Swagger·콜백 API 제공
3. **포트**: rusaint_service는 **8001**로 통일하는 것을 권장합니다. Kotlin 설정(`RUSAINT_BASE_URL`)과 맞춰야 하며, docker-compose 역시 8001을 사용합니다. 포트를 바꾸려면 아래 2.2·3.2에서 해당 포트로 맞추면 됩니다.

---

## 1. 가상환경 (rusaint_service만 해당)

rusaint_service는 Python 프로젝트이므로 **프로젝트 클론 후 최초 1회** 아래를 진행합니다.

- **가상환경 위치**: `soongpt-backend` 루트가 아니라 **`rusaint_service/` 폴더 안**에 만듭니다.
  (루트에 `.venv`를 쓰는 경우는 개인 선택이며, 가이드는 `rusaint_service/` 기준으로 통일합니다.)

```bash
# 저장소 클론 후
cd soongpt-backend/rusaint_service

# 가상환경 생성 (최초 1회)
python3 -m venv .venv

# 가상환경 활성화
# macOS / Linux:
source .venv/bin/activate

# Windows (PowerShell):
# .venv\Scripts\activate

# 의존성 설치 (최초 1회)
pip install -r requirements.txt
```

- **이후 rusaint_service 실행할 때마다**:
  `rusaint_service/` 디렉터리에서 `source .venv/bin/activate` (또는 Windows는 `.venv\Scripts\activate`) 후 uvicorn 실행하면 됩니다.

---

## 2. rusaint_service 실행

SSO sToken 검증을 위해 **반드시 먼저** 띄워 둡니다.

```bash
cd soongpt-backend/rusaint_service

# 가상환경 활성화 (위 1번 참고)
source .venv/bin/activate   # Windows: .venv\Scripts\activate

# 환경 변수 설정 (최초 1회)
cp env.example .env
# .env 에서 INTERNAL_JWT_SECRET, PSEUDONYM_SECRET 은
# soongpt-backend 루트 .env 의 RUSAINT_INTERNAL_JWT_SECRET, RUSAINT_PSEUDONYM_SECRET 과 동일하게 맞춤

# 서버 실행 (포트 8001 권장)
uvicorn app.main:app --reload --port 8001
```

- **포트**: 기본값 **8001** 사용을 권장합니다. Kotlin 앱의 `RUSAINT_BASE_URL`과 일치해야 합니다 (아래 3.2 참고).

---

## 3. soongpt-backend 로컬 서버 실행

```bash
# 저장소 루트에서
cd soongpt-backend

# dev 프로필 예시 (DB 등 환경 변수는 .env 에서 로드)
./gradlew bootRun --args='--spring.profiles.active=dev'
```

- **local** 프로필을 쓰는 경우:
  `application-local.yml`의 rusaint 기본 URL은 `http://localhost:8000`이므로, rusaint_service를 8001로 띄우면 **루트 `.env`에 다음 추가**가 필요합니다.
  `RUSAINT_BASE_URL=http://localhost:8001`
- **dev** 프로필:
  `RUSAINT_BASE_URL`을 반드시 설정해야 하며, rusaint_service 포트와 맞춥니다. 예:
  `RUSAINT_BASE_URL=http://localhost:8001`

---

## 4. Swagger에서 JWT 발급

1. 브라우저에서 **http://localhost:8080/swagger-ui** 접속
   (서버 포트를 바꿨다면 해당 호스트:포트로 접속)
2. **GET /api/sso/callback** 엔드포인트 찾기
3. **Execute** 클릭 후 파라미터 입력:
   - **sToken**: 숭실대 SSO에서 발급받은 sToken (Base64 문자열)
   - **sIdno**: 학번 (예: 20231234)
4. **Execute** 실행
5. 응답 **헤더**의 **Set-Cookie**에서 `soongpt_auth=...` 값이 **JWT**입니다.
   (302 리다이렉트가 나와도 헤더에 쿠키가 포함됩니다.)

이 JWT를 이후 API 호출 시 `Cookie: soongpt_auth=<발급받은 JWT>` 또는 Authorization 등 프로젝트 규칙에 맞게 사용하면 됩니다.

---

## 5. 포트 정리

| 구분            | 권장 포트 | 비고                                                               |
| --------------- | --------- | ------------------------------------------------------------------ |
| rusaint_service | 8001      | uvicorn `--port 8001`. Kotlin의 `RUSAINT_BASE_URL`과 일치 필요 |
| soongpt-backend | 8080      | `SERVER_PORT` 또는 기본값. Swagger UI는 여기서 제공              |

- **docker-compose** 사용 시: rusaint-service는 8001로 고정되어 있으며, soongpt는 `RUSAINT_BASE_URL=http://rusaint-service:8001`로 연결됩니다.
- **로컬에서 uvicorn + bootRun** 조합 시: rusaint_service 포트를 8001이 아닌 값(예: 8002)으로 쓰려면, soongpt-backend 루트 `.env`에 `RUSAINT_BASE_URL=http://localhost:8002`처럼 같은 포트로 설정하면 됩니다.

---

## 6. 문제 해결

- **연결 거부 / 타임아웃**: rusaint_service가 8001에서 떠 있는지, Kotlin `.env`의 `RUSAINT_BASE_URL`이 `http://localhost:8001`인지 확인.
- **sToken 만료**: SSO에서 새 sToken을 발급받아 다시 시도.
- **401 Unauthorized (rusaint 쪽)**: `rusaint_service/.env`의 `INTERNAL_JWT_SECRET`, `PSEUDONYM_SECRET`이 soongpt-backend 루트 `.env`의 `RUSAINT_INTERNAL_JWT_SECRET`, `RUSAINT_PSEUDONYM_SECRET`과 동일한지 확인.

추가 API 스펙은 [SSO 콜백 (GET /api/sso/callback)](sso_callback.md)을 참고하면 됩니다.
