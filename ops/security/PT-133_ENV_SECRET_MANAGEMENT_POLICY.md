# PT-133 환경변수(Secret) 관리 정책 수립 보고서

- 작성일: 2026-02-11
- 대상 브랜치: `feature/pt-133`
- 대상 시스템: `soongpt-backend` 배포 파이프라인 및 런타임
- 문서 성격: 보안 정책(Policy) + 적용 표준(Standard)

---

## 1) 배경 및 문제 정의

현재 배포 워크플로(`.github/workflows/dev.yml`, `prod.yml`, `deploy-only.yml`)에서 Secret 값을 CI 러너에서 `.env` 파일로 생성(`echo "KEY=$VALUE" >> .env`)한 뒤, 서버로 전송(`scp`)하는 방식이 사용되고 있다.

SSH 전송 구간 자체는 암호화되더라도, 아래 리스크가 남는다.

- Secret이 **평문 파일 형태**로 CI 러너 디스크에 기록됨
- 서버에도 `.env`가 **평문 잔존**할 수 있음
- 운영 중 디버그/오류 출력 시 Secret 노출 가능성 존재
- Secret 수명주기(회전/폐기/감사) 기준이 명확히 정의되지 않음

본 정책은 위 리스크를 줄이기 위해, 환경변수(특히 Secret) 관리 기준을 명문화한다.

---

## 2) 정책 목표

1. Secret의 평문 저장/전달 구간 최소화
2. 배포 자동화 유지하면서도 노출면(Attack Surface) 축소
3. 누가/언제/무엇에 접근했는지 감사 가능성 확보
4. 유출 사고 발생 시 신속한 회전/격리 체계 확보

---

## 3) 적용 범위

- GitHub Actions 기반 CI/CD 전 과정
- EC2(또는 동급 서버) 배포/기동 과정
- 애플리케이션 런타임 환경변수 주입 과정
- 로컬 개발 환경의 Secret 취급 기준

적용 대상 Secret 예시:

- `DB_PASSWORD`, `DB_USERNAME`, `DB_URL`
- `SLACK_TOKEN`
- `ADMIN_PASSWORD`
- `RUSAINT_PSEUDONYM_SECRET`, `RUSAINT_INTERNAL_JWT_SECRET`
- `SSO_CLIENT_JWT_SECRET`
- 기타 인증 토큰/API 키/암호성 값

---

## 4) 데이터 분류 정책

### 4.1 분류 등급

- **PUBLIC**: 외부 공개 가능
- **INTERNAL**: 내부 공유 가능 (유출 시 낮은 영향)
- **CONFIDENTIAL**: 제한된 내부 공유 (유출 시 중간 영향)
- **SECRET**: 인증/권한/암호 기능에 직접 영향 (유출 시 높은 영향)

### 4.2 강제 기준

- 위 예시 값들은 기본적으로 **SECRET**으로 분류한다.
- SECRET은 코드/문서/로그/아티팩트에 평문 저장을 금지한다.

---

## 5) 핵심 관리 정책 (MUST/SHOULD)

### 5.1 저장(Storage)

- **MUST**: SECRET의 시스템 원본 저장소를 다음 중 하나로 제한한다.
  - GitHub Environments Secrets (CI 진입점)
  - AWS Secrets Manager 또는 SSM Parameter Store(암호화 저장)
- **MUST NOT**: Git 저장소(코드/문서/예시파일), 이슈/PR 코멘트, 위키, 공유 채팅에 Secret 평문 기록 금지
- **MUST**: 서버 측 평문 파일 저장이 불가피할 경우, `chmod 600` + 소유자 제한 + 작업 종료 직후 삭제
- **SHOULD**: 장기적으로 서버 상 영구 `.env` 파일 제거 (비영구 주입 방식 전환)

### 5.2 전달(Transport)

- **MUST NOT**: CI에서 `echo "KEY=$SECRET" >> .env` 패턴으로 Secret 파일 생성/누적 금지
- **MUST NOT**: Secret을 커맨드라인 인자(프로세스 목록/히스토리 노출 가능)로 전달 금지
- **MUST**: 배포 서버가 IAM Role 기반으로 Secret 저장소에서 **직접 조회(pull)** 하도록 전환
- **SHOULD**: CI는 이미지 태그/배포 트리거만 전달하는 **Secretless Deploy** 형태로 단순화

### 5.3 사용(Runtime Injection)

- **MUST**: 컨테이너 기동 직전에 메모리/임시 파일(권한 제한)로 주입하고 즉시 파기
- **MUST**: 애플리케이션 로그/예외에서 Secret 값 출력 금지
- **SHOULD**: `/run/secrets` 또는 tmpfs 기반으로 비영구 주입

### 5.4 로깅/마스킹(Logging)

- **MUST**: GitHub Actions에서 `set -x` 사용 금지(Secret 포함 단계)
- **MUST**: 민감 값은 로그에서 마스킹 처리
- **MUST**: 에러 메시지/디버그 출력에 환경변수 덤프 금지
- **SHOULD**: 감사 로그는 "사용 사실"만 남기고 값은 남기지 않음

### 5.5 접근통제(Access Control)

- **MUST**: GitHub Environment 보호 규칙(승인자, 브랜치 제한) 적용
- **MUST**: IAM 최소권한(필요한 Secret만 읽기) 원칙 적용
- **MUST**: 운영 Secret 접근 권한은 역할 기반(RBAC)으로 부여
- **SHOULD**: break-glass 접근 절차를 별도 정의

### 5.6 회전/폐기(Rotation & Revocation)

- **MUST**: Secret 유출 의심 시 즉시 회전
- **MUST**: 정기 회전 주기 정의
  - 고위험 토큰/API 키: 최대 90일
  - DB 계정 비밀번호/내부 JWT Secret: 최대 180일
- **MUST**: 퇴사/권한변경/역할변경 시 즉시 권한 회수

### 5.7 검증/감사(Verification)

- **MUST**: PR 단계 Secret 스캔(gitleaks 등) 실행
- **MUST**: 워크플로 정적 검사로 금지 패턴 차단
  - 예: `>> .env`, `echo .*\$\{\{ secrets\.`, `set -x`
- **SHOULD**: 월 1회 Secret 접근 이력 점검, 분기 1회 정책 준수 감사

---

## 6) 권장 표준 아키텍처

1. GitHub Actions는 OIDC로 AWS Role을 획득한다.
2. CI는 서버에 이미지 태그/배포 명령만 전달한다.
3. 서버는 기동 직전 AWS SSM/Secrets Manager에서 Secret을 조회한다.
4. 조회값은 비영구 방식으로 컨테이너에 주입하고 즉시 제거한다.

핵심은 **"CI가 Secret 평문 파일을 만들어 옮기지 않는다"** 이다.

---

## 7) 이행 계획 (단계별)

### 7.1 즉시 (D+0 ~ D+7)

- 배포 워크플로에서 `.env` 생성(`echo >> .env`) 단계 제거 계획 확정
- 금지 패턴 PR 차단 규칙(간단 grep/lint) 우선 도입
- GitHub Environment 보호 규칙 점검 및 승인 플로우 강제

### 7.2 단기 (D+7 ~ D+30)

- 서버 IAM Role 기반 Secret 직접 조회 스크립트 도입
- 서버의 기존 평문 `.env` 파일 권한 정비 및 잔존 파일 정리
- Secret 스캔(gitleaks) CI 필수화

### 7.3 중기 (D+30 ~ D+60)

- Secretless Deploy 완전 전환
- 회전 자동화(주기 알림 + 점검 체크리스트) 구축
- 운영/개발 환경 분리된 Secret 정책(네이밍/권한/소유자) 정식화

---

## 8) 준수 체크리스트

- [ ] CI 워크플로에서 Secret `.env` 생성 금지 적용
- [ ] 서버 직접 Secret 조회 방식 적용
- [ ] Secret 스캔(사전/사후) 파이프라인 적용
- [ ] 환경별 접근권한 최소화 및 승인 체계 반영
- [ ] 회전 주기 등록 및 최근 회전 일자 기록
- [ ] 유출 대응 플레이북(격리/회전/공지) 문서화

---

## 9) 예외 처리

불가피하게 정책 예외가 필요한 경우, 아래 항목을 사전 승인받아야 한다.

- 예외 사유
- 적용 범위/기간(만료일 필수)
- 대체 통제 수단
- 종료 후 원복 계획

승인 없이 예외 적용 금지.

---

## 10) 결론

현재 방식은 자동화 관점에서는 단순하지만, Secret이 평문으로 생성·저장·전달되는 구간이 존재한다. 본 정책의 핵심은 **Secret의 평문 상주 시간을 최소화하고, 전달 주체를 CI에서 서버의 권한 기반 조회 방식으로 전환**하는 것이다.

`feature/pt-133`에서는 우선 정책 문서화를 완료하고, 후속 작업으로 워크플로/배포 스크립트 개선을 단계적으로 적용한다.
