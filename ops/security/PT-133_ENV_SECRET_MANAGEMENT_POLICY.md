# PT-133 환경변수/비밀키(Secret) 관리 정책 보고서 (개정안 v2)

- 작성일: 2026-02-11
- 대상 브랜치: `feature/pt-133`
- 대상 시스템: `soongpt-backend` (로컬 개발 + CI/CD + 서버 런타임)
- 개정 사유: **로컬 dev 실행 시 평문 `.env` 저장 리스크를 핵심 이슈로 재정의**하고, 중앙 집중식 비밀키 관리 시스템 도입 방안을 포함해 정책 재작성

---

## 1) 요청사항 반영 요약

이번 개정안은 다음 요청을 직접 반영한다.

1. 주요 보안 문제를 **로컬 환경의 평문 `.env` 저장**으로 설정
2. **중앙 집중식 비밀키 관리 시스템**(Centralized Secret Management) 검토
3. 실제 적용 가능한 방법(보안성/운영복잡도/비용/개발 생산성 균형) 제시

---

## 2) 현황 진단 (코드/설정 근거)

로컬 개발에서 Secret 취급과 관련된 현재 상태는 다음과 같다.

- `examples/local.env.example`
  - `DB_PASSWORD`, `ADMIN_PASSWORD`, `SSO_CLIENT_JWT_SECRET` 등 민감값 필드 존재
  - 로컬 실행 가이드에서 `.env` 복사 후 수정하는 방식 사용
- `docker-compose.local.yml`, `docker-compose.yml`
  - `${VAR}` 형태 환경변수 주입 구조 사용
- `src/main/resources/application-local.yml`
  - 일부 기본값은 존재하나, 실제 연동 시 Secret 주입 필요
- `.gitignore`
  - `.env`, `.env.*`는 제외 처리되어 있으나, **로컬 디스크에 평문 보관되는 문제는 여전히 남음**

즉, "Git 유출"은 줄였지만 "엔드포인트(개발자 PC) 평문 상주" 리스크는 해결되지 않은 상태다.

---

## 3) 핵심 보안 문제: 로컬 평문 `.env` 상주

### 3.1 위협 시나리오

- 악성코드/정보탈취형 프로그램이 홈 디렉토리의 `.env` 수집
- 백업/동기화 도구(클라우드 드라이브, 스냅샷)에 평문 포함
- 원격 지원/화면 공유 중 파일 노출
- 단순 오조작(복사/붙여넣기, 로그 출력)으로 Secret 외부 유출
- 퇴사/단말 분실 시 로컬 디스크에 잔존한 Secret 악용

### 3.2 영향

- DB 접근정보 유출 시 데이터 유출/변조 위험
- 내부 JWT/SSO Secret 유출 시 인증 위조 위험
- 운영/개발 환경 간 동일 Secret 재사용 시 횡적 확산 위험

---

## 4) 중앙 집중식 비밀키 관리 시스템 검토

본 프로젝트는 이미 AWS + GitHub OIDC 기반 배포 체계를 사용하고 있어, AWS 네이티브 선택지가 가장 합리적이다.

### 후보 A) AWS SSM Parameter Store (SecureString)

- 장점
  - AWS 생태계와 결합 용이, IAM 최소권한 구성 쉬움
  - 정적 Secret 관리에 비용 효율적
  - 경로 기반 네임스페이스(`/soongpt/dev/...`) 운영에 적합
- 단점
  - 자동 회전 기능은 직접 설계 필요

### 후보 B) AWS Secrets Manager

- 장점
  - 자동 회전(특히 DB credential) 기능 강함
  - Secret 버전/관리 기능이 풍부
- 단점
  - Parameter Store 대비 비용 증가
  - 단순 key-value 대량 관리 시 과투자 가능

### 후보 C) HashiCorp Vault (Self-managed)

- 장점
  - 정책/감사/동적 크레덴셜 고도화 가능
- 단점
  - 운영 복잡도 매우 높음(HA, 백업, 업그레이드, 운영인력 필요)
  - 현재 팀 규모/운영 성숙도 대비 부담이 큼

### 후보 D) Doppler/1Password Secrets Automation 등 SaaS

- 장점
  - 개발자 UX 우수, 로컬 주입 편의성 높음
- 단점
  - 외부 SaaS 의존성/비용/컴플라이언스 검토 필요
  - 기존 AWS 중심 체계와 권한모델 이중화 가능성

---

## 5) 권고안 (합리적 선택)

### 5.1 권고 아키텍처

**단기~중기 권고: "SSM Parameter Store + (필요 시 일부) Secrets Manager" 하이브리드**

- 기본: 대부분 Secret은 SSM SecureString에 통합
- 회전 자동화가 중요한 항목(DB 계정 등)은 Secrets Manager 검토
- 서버/로컬 모두 "필요 시점 조회" 원칙으로 주입

### 5.2 왜 이 선택이 합리적인가

- 현재 인프라(AWS/OIDC)와 가장 자연스럽게 결합
- 보안 수준 대비 도입 난이도/비용 균형이 좋음
- Vault 수준의 고복잡 플랫폼을 당장 운영하지 않아도 실질 위험을 크게 줄일 수 있음

---

## 6) 정책 (개정)

### 6.1 공통 정책

- **MUST**: Secret의 단일 원본(Source of Truth)을 중앙 저장소(SSM/Secrets Manager)로 통합
- **MUST**: Secret 평문을 코드/문서/로그/채팅/이슈에 기록 금지
- **MUST**: 환경별(dev/stage/prod) Secret 완전 분리
- **MUST**: IAM 최소권한(RBAC) 적용, 접근 로그 감사 가능 상태 유지
- **MUST**: Secret 유출 의심 시 즉시 회전 및 영향범위 분석

### 6.2 로컬 개발 정책 (핵심)

- **MUST NOT**: 실제 민감 Secret을 장기 보관 `.env` 파일로 로컬 디스크에 저장 금지
- **MUST**: 로컬에서 실제 Secret이 필요한 경우 중앙 저장소에서 **세션 단위(on-demand) 조회**
- **MUST**: 주입 방식은 메모리 우선(쉘 환경변수), 파일이 필요하면 임시파일 + 즉시 삭제
- **SHOULD**: 기본 로컬 개발은 H2/더미 값 기반으로 동작시켜 실제 Secret 사용 빈도 최소화

### 6.3 CI/CD 및 서버 정책

- **MUST NOT**: GitHub Actions에서 `echo "KEY=$VALUE" >> .env` 형태로 Secret 파일 생성/전송 금지
- **MUST**: 배포 서버가 IAM Role로 중앙 저장소에서 직접 조회
- **MUST**: 서버 영구 `.env` 저장 최소화(불가피 시 권한 `600`, 수명 제한, 주기적 정리)

---

## 7) 적용 방법 (실행 설계)

### 7.1 Secret 네이밍 표준 수립

예시 경로:

- `/soongpt/dev/DB_URL`
- `/soongpt/dev/DB_USERNAME`
- `/soongpt/dev/DB_PASSWORD`
- `/soongpt/dev/ADMIN_PASSWORD`
- `/soongpt/dev/SSO_CLIENT_JWT_SECRET`
- `/soongpt/dev/RUSAINT_INTERNAL_JWT_SECRET`

원칙:

- 환경별 prefix 분리: `/soongpt/{env}/...`
- 키 이름 일관성 유지(앱 환경변수 명과 1:1 대응)

### 7.2 IAM 최소권한 정책

- 개발자 로컬: `/soongpt/dev/*` 읽기만 허용
- 배포 서버(EC2 Role): 해당 환경 prefix만 읽기 허용
- 운영 접근은 승인된 역할로 제한

### 7.3 로컬 실행 방식 (권장)

### 모드 A: 일반 개발(권장 기본)

- `application-local.yml` + 더미값/H2로 기능 개발
- 실제 외부 연동이 없으면 Secret 조회 자체를 생략

### 모드 B: 연동 검증(실제 Secret 필요 시)

- 중앙 저장소에서 세션 단위로 읽어 쉘 환경변수로 export
- 세션 종료 시 자동 unset
- 디스크 상 장기 `.env` 파일 미생성

샘플(개념) 스크립트:

```bash
#!/usr/bin/env bash
set -euo pipefail

PREFIX="/soongpt/dev"
KEYS=(
  DB_URL DB_USERNAME DB_PASSWORD
  ADMIN_PASSWORD
  SSO_CLIENT_JWT_SECRET
  RUSAINT_PSEUDONYM_SECRET
  RUSAINT_INTERNAL_JWT_SECRET
)

for k in "${KEYS[@]}"; do
  v=$(aws ssm get-parameter \
    --name "$PREFIX/$k" \
    --with-decryption \
    --query 'Parameter.Value' \
    --output text)
  export "$k=$v"
done

# 파일 없이 바로 compose 실행
# (docker compose는 현재 쉘 환경변수 사용)
docker compose -f docker-compose.local.yml up -d
```

> 참고: 디버그(`set -x`) 금지. 필요 시 종료 훅에서 `unset` 처리.

### 모드 C: 파일이 꼭 필요할 때(차선)

- `mktemp` 임시파일 생성 + `chmod 600`
- 실행 후 즉시 삭제(`trap ... rm -f`)
- 사용자 홈 디렉토리 고정 `.env`는 금지

### 7.4 배포 파이프라인 개선

현재 워크플로(`dev.yml`, `prod.yml`, `deploy-only.yml`)의 `.env` 생성/전송 구간을 제거하고,
다음 구조로 전환한다.

1. GitHub Actions: 이미지 빌드/푸시 + 배포 트리거만 수행
2. 서버: 배포 스크립트가 SSM/Secrets Manager에서 Secret 직접 조회
3. 런타임: 조회값으로 컨테이너 기동, 필요 시 임시파일 즉시 파기

---

## 8) 단계별 이행 계획

### Phase 1 (1주 이내)

- 정책 확정 및 Secret 인벤토리 작성
- 중앙 저장소(SSM) 경로 설계/등록
- 로컬 `.env` 장기보관 금지 규칙 공지

### Phase 2 (2~3주)

- 로컬용 `load-dev-secrets` 스크립트 도입
- CI에 Secret 스캔(gitleaks) + 금지패턴 차단 추가
  - 금지 예: `>> .env`, `set -x`, Secret echo 출력

### Phase 3 (4~6주)

- 배포 파이프라인의 Secretless 전환 완료
- 서버 직접 조회 구조 정착
- 회전 주기 운영(예: 고위험 90일, 기타 180일)

---

## 9) 검증 기준 (완료 정의)

다음을 만족하면 정책 적용 완료로 본다.

- [ ] 로컬에 장기 보관되는 실제 Secret `.env` 파일 제거
- [ ] 로컬 연동 검증 시 중앙 저장소 on-demand 조회 방식 적용
- [ ] CI에서 Secret 평문 파일 생성/전송 로직 제거
- [ ] 서버 IAM Role 기반 Secret 조회 적용
- [ ] Secret 스캔 및 금지패턴 검사 CI 반영
- [ ] 회전/유출 대응 절차 문서화

---

## 10) 결론

현재 가장 큰 실질 리스크는 "로컬 dev에서 실제 Secret이 평문 `.env`로 장기 상주"하는 점이다.

따라서 본 개정안은 중앙 집중식 비밀키 관리(우선 SSM 기반)를 도입하고,
로컬/서버 모두 **필요 시점 조회 + 비영구 주입**으로 전환하는 것을 핵심으로 한다.

이 방식은 보안성 향상 폭이 크면서도, 기존 AWS 기반 운영체계와 충돌이 적고 도입 난이도도 현실적이다.
