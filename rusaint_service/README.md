# Rusaint Service

FastAPI 기반의 u-saint 데이터 크롤링 서비스입니다. [rusaint](https://github.com/EATSTEAK/rusaint) 라이브러리를 사용하여 숭실대학교 유세인트 시스템에서 학적/성적 정보를 조회합니다.

## 주요 기능

- **SSO 토큰 기반 인증**: u-saint SSO 토큰을 사용하여 세션 생성
- **학적 정보 조회**: 학년, 학기, 학과 등 기본 학적 정보
- **수강 내역 조회**: 학기별 수강 과목 코드 목록 (계절학기 포함)
- **저성적 과목 분류**: C/D/F 성적 과목을 이수구분별로 자동 분류
- **졸업 요건 조회**: 남은 졸업 이수 학점 정보
- **복수전공/교직 정보**: 복수전공, 부전공, 교직 이수 여부
- **내부 JWT 인증**: WAS와의 안전한 통신을 위한 JWT 검증

## 환경 설정

### 1. 환경 변수 설정

`env.example` 파일을 `.env`로 복사하고 값을 설정합니다:

```bash
cp env.example .env
```

**필수 환경 변수:**

```bash
# 내부 JWT 인증 (필수 - WAS와 동일한 시크릿 사용)
INTERNAL_JWT_SECRET=your-secret-key-here

# 개발 모드 (프로덕션에서는 false)
DEBUG=true
```

**선택 환경 변수:**

```bash
# Rusaint 설정
RUSAINT_TIMEOUT=30  # 초 단위

# CORS 설정 (쉼표로 구분)
ALLOWED_ORIGINS=http://localhost:8080,http://localhost:3000

# Redis 설정 (Phase 3)
REDIS_HOST=localhost
REDIS_PORT=6379
```

### 2. 의존성 설치

```bash
# 가상 환경 생성
python3 -m venv .venv
source .venv/bin/activate  # Windows: .venv\Scripts\activate

# 의존성 설치
pip install -r requirements.txt
```

### 3. 서버 실행

```bash
# 개발 서버 (자동 리로드)
uvicorn app.main:app --reload --port 8001

# 프로덕션 서버
uvicorn app.main:app --host 0.0.0.0 --port 8001 --workers 4
```

## API 엔드포인트

### 1. 헬스체크

```bash
# 기본 헬스체크
GET /api/health

# Readiness 체크 (의존성 확인)
GET /api/health/ready
```

### 2. 유세인트 데이터 조회 (내부 API)

```bash
POST /api/usaint/snapshot
```

**요청 헤더:**
- `Authorization: Bearer {internal-jwt}`
- `Content-Type: application/json`

**요청 본문:**
```json
{
  "studentId": "20231234",
  "sToken": "MYSAPSSO2=..."
}
```

**응답 예시:**
```json
{
  "pseudonym": "base64url_hmac_sha256_of_student_id",
  "takenCourses": [
    {
      "year": 2024,
      "semester": "1",
      "subjectCodes": ["2150545501", "2150545502"]
    }
  ],
  "lowGradeSubjectCodes": ["2150545501"],
  "flags": {
    "doubleMajorDepartment": null,
    "minorDepartment": null,
    "teaching": false
  },
  "basicInfo": {
    "year": 2023,
    "grade": 2,
    "semester": 4,
    "department": "컴퓨터학부"
  }
}
```

## 테스트

```bash
# 전체 테스트 실행
pytest

# 커버리지 포함
pytest --cov=app --cov-report=html

# 특정 테스트 실행
pytest tests/test_security.py -v
```

## 프로젝트 구조

```
rusaint_service/
├── app/
│   ├── api/
│   │   ├── __init__.py
│   │   └── usaint_router.py      # API 라우터
│   ├── core/
│   │   ├── __init__.py
│   │   ├── config.py              # 설정 관리
│   │   └── security.py            # 내부 JWT 검증
│   ├── schemas/
│   │   ├── __init__.py
│   │   └── usaint_schemas.py      # Pydantic 스키마
│   ├── services/
│   │   ├── __init__.py
│   │   └── rusaint_service.py     # 비즈니스 로직
│   └── main.py                    # FastAPI 앱
├── tests/
│   ├── __init__.py
│   ├── conftest.py
│   ├── test_config.py
│   ├── test_security.py
│   └── test_rusaint_service.py
├── env.example                    # 환경 변수 예시
├── requirements.txt               # 의존성
└── README.md
```

## 보안 고려사항

1. **개인정보 보호**
   - 학번은 로그에서 일부 마스킹 처리 (`2023****`)
   - 성적 절대값(A+, B+ 등)은 저장하지 않음
   - 에러 로그에서 민감 정보 제외 (`error_type`만 기록)

2. **내부 JWT 인증**
   - WAS와 동일한 시크릿 사용
   - 개발 모드에서만 placeholder 토큰 허용
   - 프로덕션 환경에서는 JWT 시크릿 필수

3. **CORS 설정**
   - 허용된 origin만 접근 가능
   - 프로덕션에서는 WAS origin만 허용

## 성능 최적화

1. **API 호출 최적화**
   - 졸업요건 API를 한 번만 호출하여 재사용
   - 불필요한 상세 정보는 조회하지 않음

2. **세션 관리**
   - 사용 후 세션 명시적 종료
   - 에러 발생 시에도 세션 정리 보장

3. **타임아웃 설정**
   - rusaint API 호출에 타임아웃 적용 (기본 30초)
   - 환경 변수로 조정 가능

## 문제 해결

### 1. SSO 토큰 만료

**증상:** `401 Unauthorized - SSO token is invalid or expired`

**해결:** 클라이언트에서 새로운 SSO 토큰을 발급받아 재시도

### 2. 타임아웃 오류

**증상:** `유세인트 연결 시간이 초과되었습니다`

**해결:** `RUSAINT_TIMEOUT` 환경 변수 값을 증가시킴

### 3. 의존성 오류

**증상:** `rusaint library not available`

**해결:**
```bash
pip install --upgrade rusaint
```

## 라이센스

이 프로젝트는 [rusaint](https://github.com/EATSTEAK/rusaint) 라이브러리를 사용합니다.
