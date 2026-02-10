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

## 4) 현재 컴퓨터용 Nginx 템플릿 적용

템플릿 파일:

- `ops/nginx/soongpt.current-host.conf.template`

자동 설치 스크립트:

- `ops/nginx/install-current-host.sh`

적용 커맨드:

```bash
cd /path/to/soongpt-backend
SERVER_NAME=<도메인 또는 _> SERVER_PORT=8080 ./ops/nginx/install-current-host.sh
```

예시:

```bash
SERVER_NAME=api.soongpt.com SERVER_PORT=8080 ./ops/nginx/install-current-host.sh
```

검증:

```bash
sudo nginx -t
curl -I http://127.0.0.1:8080/actuator/health
curl -I http://<SERVER_NAME>/actuator/health
```

---

## 5) GitHub Actions와의 연결 포인트

- CI에서 이미지를 push하면 (`dev.yml`, `prod.yml`) 서버는 `docker-compose.deploy.yml` 기준으로 재기동
- 롤백 시에는 `deploy-only.yml` 또는 서버에서 `IMAGE_TAG=<old-sha>`로 재배포
- 운영 안정성을 위해 `latest` 고정 대신 SHA 태그 기준 배포 권장
