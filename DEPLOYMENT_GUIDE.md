# SoongPT Backend 배포 가이드 (로컬/서버 분리)

## 1) Compose 분리 구조

- `docker-compose.local.yml`
  - 로컬 개발용
  - `build: .` / `build: ./rusaint_service`로 소스에서 직접 이미지 빌드
- `docker-compose.deploy.yml`
  - 서버 배포용
  - ECR 이미지(`image:`)를 pull 해서 실행
- `docker-compose.yml`
  - 로컬 개발용 별칭(alias). 기존 사용 호환용

---

## 2) 로컬 실행 커맨드

```bash
cd /path/to/soongpt-backend
cp examples/local.env.example .env
# .env 값 수정

docker compose -f docker-compose.local.yml up -d --build
```

상태/로그/중지:

```bash
docker compose -f docker-compose.local.yml ps
docker compose -f docker-compose.local.yml logs -f soongpt
docker compose -f docker-compose.local.yml down
```

---

## 3) 서버(배포) 실행 커맨드

```bash
cd /home/ubuntu/soongpt-api
cp /path/to/repo/examples/deploy.env.example .env
# .env 값 수정 (ECR_REGISTRY, DB, SECRET 등)

docker compose -f docker-compose.deploy.yml pull
docker compose -f docker-compose.deploy.yml up -d
```

상태/로그/중지:

```bash
docker compose -f docker-compose.deploy.yml ps
docker compose -f docker-compose.deploy.yml logs -f soongpt
docker compose -f docker-compose.deploy.yml down
```

특정 태그 배포:

```bash
IMAGE_TAG=<git-sha-or-tag> docker compose -f docker-compose.deploy.yml up -d
```

---

## 4) 현재 컴퓨터용 Nginx + HTTPS(Let's Encrypt) 적용

템플릿 파일:

- `ops/nginx/soongpt.current-host.conf.template`

자동 설치 스크립트:

- HTTP만 먼저 적용: `ops/nginx/install-current-host.sh`
- HTTPS(Let's Encrypt)까지 적용: `ops/nginx/enable-https-letsencrypt.sh`

적용 커맨드 (권장):

```bash
cd /path/to/soongpt-backend
SERVER_NAME=api.backup.soongpt.yourssu.com SERVER_PORT=9001 LETSENCRYPT_EMAIL=<email> ./ops/nginx/enable-https-letsencrypt.sh
```

HTTP만 테스트할 때:

```bash
SERVER_NAME=api.backup.soongpt.yourssu.com SERVER_PORT=9001 ./ops/nginx/install-current-host.sh
```

검증:

```bash
sudo nginx -t
curl -I http://127.0.0.1:9001/actuator/health
curl -I https://api.backup.soongpt.yourssu.com/actuator/health
```

---

## 5) GitHub Actions 환경 설정 (dev-backup)

- `dev.yml`은 기존대로 `dev` 환경 자동 배포용
- `dev-backup.yml`은 `dev-backup` 환경 수동 배포 전용 (`workflow_dispatch`)

### Variables (Environment: dev-backup)

자동 동기화 스크립트:

```bash
REPO=yourssu/soongpt-backend SOURCE_ENV=dev TARGET_ENV=dev-backup HOST_URL_OVERRIDE=api.backup.soongpt.yourssu.com ./ops/github/sync-env-vars.sh
```

- `HOST_URL=api.backup.soongpt.yourssu.com`
- `SERVER_PORT=9001`
- `PROJECT_NAME=soongpt`
- `ENVIRONMENT=dev`
- `ECR_PUBLIC_REGISTRY_ID`
- `CORS_ALLOWED_ORIGIN`
- `SLACK_CHANNEL`
- `SLACK_LOG_CHANNEL`
- `SSO_FRONTEND_URL`
- `SSO_ALLOWED_REDIRECT_URLS`

### Secrets (Environment: dev-backup)

아래 키들을 `dev`와 동일 값으로 반드시 등록:
(주의: GitHub 정책상 기존 환경 secret 값을 조회할 수 없어 수동 복사 필요)

- `HOME_PEM`
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `ADMIN_PASSWORD`
- `SLACK_TOKEN`
- `RUSAINT_PSEUDONYM_SECRET`
- `RUSAINT_INTERNAL_JWT_SECRET`
- `SSO_CLIENT_JWT_SECRET`

### Dev-backup 수동 배포

`Actions > Deploy Only (Dev-Backup)`에서 실행.

(`deploy-only.yml`은 기존 dev/prod 선택용으로 유지)

---

## 6) 운영 팁

- CI에서 이미지를 push하면 (`dev.yml`, `prod.yml`) 서버는 `docker-compose.deploy.yml` 기준으로 재기동
- 롤백 시에는 `deploy-only.yml` 또는 서버에서 `IMAGE_TAG=<old-sha>`로 재배포
- 운영 안정성을 위해 `latest` 고정 대신 SHA 태그 기준 배포 권장
